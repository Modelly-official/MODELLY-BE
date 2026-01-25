package modelly.modelly_be.domain.auth.service;

import com.google.maps.model.LatLng;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import modelly.modelly_be.domain.auth.dto.internal.*;
import modelly.modelly_be.domain.auth.dto.request.SocialSignupRequest;
import modelly.modelly_be.domain.auth.dto.request.LoginRequest;
import modelly.modelly_be.domain.auth.dto.request.SignupRequest;
import modelly.modelly_be.domain.auth.dto.response.*;
import modelly.modelly_be.domain.user.entity.*;
import modelly.modelly_be.domain.user.entity.enums.LoginType;
import modelly.modelly_be.domain.user.entity.enums.Permission;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.repository.ModelRepository;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.geocoding.GeoCodingService;
import modelly.modelly_be.global.security.entity.TokenStatus;
import modelly.modelly_be.global.security.google.GoogleDTO;
import modelly.modelly_be.global.security.google.GoogleUtil;
import modelly.modelly_be.global.security.kakao.KakaoDTO;
import modelly.modelly_be.global.security.kakao.KakaoUtil;
import modelly.modelly_be.global.security.naver.NaverDTO;
import modelly.modelly_be.global.security.naver.NaverUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import modelly.modelly_be.domain.user.repository.designerRepository.DesignerRepository;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.redis.RedisService;
import modelly.modelly_be.global.security.dto.TokenResponse;
import modelly.modelly_be.global.security.jwt.TokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String RT_KEY_PREFIX = "refresh-token:";
    private static final long OVERLAP_TTL_SECONDS = 10; // refresh token overlap 시간 (10초)

    private final UserRepository userRepository;
    private final DesignerRepository designerRepository;
    private final ModelRepository modelRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final KakaoUtil kakaoUtil;
    private final NaverUtil naverUtil;
    private final GoogleUtil googleUtil;
    private final GeoCodingService geoCodingService;
    private final DesignerService designerService;

    /* ---------- JWT 회원가입/로그인/로그아웃 ---------- */

    /* 회원가입 */
    @Transactional
    public SignupResponse signup(SignupRequest req) {
        var base = req.getBase(); // 공통 항목

        // 중복 체크(login_id, email)
        if (userRepository.existsByLoginId(base.getLoginId()))
            throw new GeneralException(ErrorStatus.DUPLICATE_LOGIN_ID);

        if (base.getEmail() != null && userRepository.existsByEmail(base.getEmail()))
            throw new GeneralException(ErrorStatus.DUPLICATE_EMAIL);

        // 유저 생성
        User user = User.builder()
                .loginId(base.getLoginId())
                .password(passwordEncoder.encode(base.getPassword()))
                .email(base.getEmail())
                .name(base.getName())
                .phoneNum(base.getPhoneNum())
                .gender(base.getGender())
                .birth(base.getBirth())
                .imageUrl(base.getImageUrl())
                .loginType(LoginType.JWT)
                .userRole(base.getUserRole())
                .permission(Permission.USER)
                .build();

        user.createNotificationSetting();

        userRepository.save(user);

        // Designer, Model 정보 받아옴
        DesignerExtra designerExtra = req.getDesigner();
        ModelExtra modelExtra = req.getModel();
        String nickname = null;

        UserRole role = base.getUserRole();
        // UserRole에 맞는 입력 정보인지 확인
        if (role == UserRole.DESIGNER) {
            // 디자이너인데 model 정보가 오거나, designer 정보가 없으면 에러
            if (designerExtra == null || modelExtra != null) {
                throw new GeneralException(ErrorStatus.SIGNUP_FIELDS_ERROR);
            }

            LatLng shopLocation = geoCodingService.getLatLngRes(designerExtra.getAddressLine1()+" "+designerExtra.getAddressLine2());

            if (shopLocation == null) {
                throw new GeneralException(ErrorStatus.GEOCODING_FAILED);
            }

            Designer designer = Designer.builder()
                    .user(user)
                    .shop(designerExtra.getShop())
                    .addressLine1(designerExtra.getAddressLine1())
                    .addressLine2(designerExtra.getAddressLine2())
                    .latitude(shopLocation.lat)
                    .longitude(shopLocation.lng)
                    .category(designerExtra.getCategory())
                    .chemistryScore(0L)
                    .nickname(designerExtra.getNickname())
                    .instagramId(null)
                    .intro(designerExtra.getIntro())
                    .build();

            designerRepository.save(designer);
            nickname = designer.getNickname();

        } else if (role == UserRole.MODEL) {
            // 모델인데 designer 정보가 오거나, model 정보가 없으면 에러
            if (modelExtra == null || designerExtra != null) {
                throw new GeneralException(ErrorStatus.SIGNUP_FIELDS_ERROR);
            }

            Model model = Model.of(user, modelExtra.getNickname());
            modelRepository.save(model);
            nickname = model.getNickname();

        }

        return SignupResponse.of("회원가입이 완료되었습니다.", user.getLoginId(), user.getName(), nickname, user.getUserRole().getDescription());
    }

    /* 로그인 */
    @Transactional
    public LoginResult login(LoginRequest req) {
        User user = userRepository.findByLoginId(req.getLoginId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.LOGIN_FAIL));

        // 탈퇴한 회원인지 확인
        if (user.getDeletedAt() != null) {
            user.recoverAccount();
        }

        Designer designer = null;
        if (user.getUserRole()==UserRole.DESIGNER) {
            designer = designerService.getByUser(user);
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new GeneralException(ErrorStatus.LOGIN_FAIL);

        TokenResponse tokens = tokenProvider.createToken(user);

        // refresh token
        String refreshToken = tokens.getRefreshToken();

        // userId 기준으로 레디스에 refresh token 저장(TTL=refresh 남은 시간)
        long ttlSec = tokenProvider.getRemainingSeconds(tokens.getRefreshToken());

        // current key 저장
        String currentKey = RT_KEY_PREFIX + user.getId() + ":current";

        redisService.setRefreshToken(
                currentKey,
                refreshToken,
                ttlSec
        );

        // 로그인 응답 생성
        LoginResponse loginResponse = LoginResponse.of(
                user.getId(),
                tokens.getAccessToken(),
                user.getUserRole().getDescription(),
                user.getUserRole() == UserRole.DESIGNER ? designer.getCategory().getDescription() : null
        );

        return LoginResult.of(loginResponse, refreshToken, ttlSec);
    }

    /* 로그아웃 */
    @Transactional
    public SimpleMessageDTO logout(HttpServletRequest request) {
        String accessToken = tokenProvider.resolveToken(request);

        if (accessToken == null || accessToken.isBlank())
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);

        String userId = tokenProvider.getUserIdFromToken(accessToken);

        // current, previous key 둘 다 삭제
        redisService.deleteValue(RT_KEY_PREFIX + userId + ":current");
        redisService.deleteValue(RT_KEY_PREFIX + userId + ":previous");

        return new SimpleMessageDTO("로그아웃이 완료되었습니다.");
    }


    /* ---------- Access Token 재발급 및 유효 여부 확인 ---------- */

    /* Access token 재발급 */
    @Transactional(readOnly = true)
    public NewTokenResult newAccessToken(String refreshToken) {

        // refresh 토큰 검증
        TokenStatus tokenStatus = tokenProvider.validateToken(refreshToken);
        if(tokenStatus == TokenStatus.EXPIRED){
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }
        else if(tokenStatus == TokenStatus.INVALID || tokenStatus == TokenStatus.MISSING){
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);
        }

        // user 확인
        String userId = tokenProvider.getUserIdFromToken(refreshToken);

        // Redis의 refresh 토큰과 비교
        String currentKey = RT_KEY_PREFIX + userId + ":current";
        String previousKey = RT_KEY_PREFIX + userId + ":previous";

        String current = (String) redisService.getValue(currentKey);
        String previous = (String) redisService.getValue(previousKey);

        boolean valid = refreshToken.equals(current) || refreshToken.equals(previous);

        if (!valid) {
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);
        }

        // user 조회
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        if (user.getDeletedAt() != null) {
            throw new GeneralException(ErrorStatus.WITHDRAWN_USER);
        }

        //  Access, Refresh 발급
        String newAccess = tokenProvider.createAccessToken(user);
        String newRefresh = tokenProvider.createRefreshToken(user);

        // Redis에 새 refresh 저장
        long refreshTtl = tokenProvider.getRemainingSeconds(newRefresh);

        // current를 previous로 설정
        if (current != null && !current.isEmpty()) {
            redisService.setRefreshToken(
                    previousKey,
                    current,
                    OVERLAP_TTL_SECONDS
            );
        }

        redisService.setRefreshToken(
                currentKey,
                newRefresh,
                refreshTtl
        );

        return NewTokenResult.of(AccessTokenResponse.of(newAccess), newRefresh, refreshTtl);
    }

    /* Access Token 유효 여부 확인 */
    @Transactional(readOnly = true)
    public TokenValidationResponse isValidAccess(HttpServletRequest request) {
        // 유효성 검사는 JwtAuthenticationFilter에서 처리
        String accessToken = tokenProvider.resolveToken(request);

        if (accessToken == null || accessToken.isBlank())
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);

        TokenStatus status = tokenProvider.validateToken(accessToken);

        return TokenValidationResponse.of("Access Token의 상태는 다음과 같습니다.", status);
    }


    /* ---------- 아이디/이메일 중복 체크 ---------- */

    /* 로그인 아이디 중복 체크 */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkLoginId(String value) {
        boolean available = !userRepository.existsByLoginId(value);
        return DuplicateCheckResponse.of("loginId", value, available);
    }

    /* 이메일 중복 체크 */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkEmail(String value) {
        boolean available = !userRepository.existsByEmail(value);
        return DuplicateCheckResponse.of("email", value, available);
    }

    /* ---------- 소셜 로그인/회원가입 ----------*/

    /* 소셜 로그인 회원가입 */
    @Transactional
    public SignupResponse SocialSignup(HttpServletRequest request, SocialSignupRequest req) {

        // access token 검증
        String accessToken = tokenProvider.resolveToken(request);
        if (accessToken == null || accessToken.isBlank())
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);

        // 토큰에서 userId 추출
        String userIdStr = tokenProvider.getUserIdFromToken(accessToken);
        Long userId = Long.valueOf(userIdStr);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        // 회원가입 중복 호출 방지
        boolean alreadyCompleted = designerRepository.existsByUser_Id(userId) || modelRepository.existsByUser_Id(userId);
        if (alreadyCompleted) {
            throw new GeneralException(ErrorStatus.DUPLICATE_USER_REGISTERED);
        }

        var base = req.getBase();

        user.updateFromSocialSignup(base);

        // Designer / Model 정보 저장
        DesignerExtra designerExtra = req.getDesigner();
        ModelExtra modelExtra = req.getModel();

        if (designerExtra != null && modelExtra != null) {
            throw new GeneralException(ErrorStatus.SIGNUP_FIELDS_ERROR);
        }

        String nickname = null;
        UserRole role = base.getUserRole();

        if (role == UserRole.DESIGNER) {
            if (designerExtra == null || modelExtra != null) {
                throw new GeneralException(ErrorStatus.SIGNUP_FIELDS_ERROR);
            }

            LatLng shopLocation = geoCodingService.getLatLngRes(designerExtra.getAddressLine1()+" "+designerExtra.getAddressLine2());

            if (shopLocation == null) {
                throw new GeneralException(ErrorStatus.GEOCODING_FAILED);
            }

            Designer designer = Designer.builder()
                    .user(user)
                    .shop(designerExtra.getShop())
                    .addressLine1(designerExtra.getAddressLine1())
                    .addressLine2(designerExtra.getAddressLine2())
                    .latitude(shopLocation.lat)
                    .longitude(shopLocation.lng)
                    .category(designerExtra.getCategory())
                    .chemistryScore(0L)
                    .nickname(designerExtra.getNickname())
                    .instagramId(null)
                    .intro(designerExtra.getIntro())
                    .build();

            designerRepository.save(designer);
            nickname = designer.getNickname();

        } else if (role == UserRole.MODEL) {
            if (modelExtra == null || designerExtra != null) {
                throw new GeneralException(ErrorStatus.SIGNUP_FIELDS_ERROR);
            }

            Model model = Model.of(user, modelExtra.getNickname());
            modelRepository.save(model);
            nickname = model.getNickname();

        }

        return SignupResponse.of("회원가입이 완료되었습니다.", user.getEmail(), user.getName(), nickname, user.getUserRole().getDescription());
    }

    /* 카카오 로그인(토큰, 회원가입 여부 반환) */
    @Transactional
    public SocialLoginResult kakaoLogin(String code) {
        // 카카오 토큰으로 프로필 조회
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(code);
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        var kakaoAccount = kakaoProfile.getKakao_account();
        if (kakaoAccount == null || kakaoAccount.getEmail() == null) {
            throw new GeneralException(ErrorStatus.SOCIAL_PROFILE_INCOMPLETE);
        }

        // 카카오로부터 이메일, 이름 정보 받아오기
        String kakaoEmail = kakaoAccount.getEmail();
        String kakaoName = kakaoProfile.getKakao_account().getProfile().getNickname();

        return processSocialLogin(kakaoEmail, kakaoName, LoginType.KAKAO);
    }

    /* 네이버 로그인(토큰, 회원가입 여부 반환) */
    @Transactional
    public SocialLoginResult naverLogin(String code, String state) {

        // 네이버 토큰, 프로필 조회
        NaverDTO.OAuthToken oAuthToken = naverUtil.requestToken(code, state);
        NaverDTO.NaverProfile profile = naverUtil.requestProfile(oAuthToken);
        NaverDTO.NaverProfile.Response res = profile.getResponse();

        if (res == null || res.getEmail() == null) {
            throw new GeneralException(ErrorStatus.SOCIAL_PROFILE_INCOMPLETE);
        }

        String email = res.getEmail();
        String name = res.getName() != null ? res.getName() : res.getNickname();
        if (name == null) {
            throw new GeneralException(ErrorStatus.SOCIAL_PROFILE_INCOMPLETE);
        }

        return processSocialLogin(email, name, LoginType.NAVER);
    }

    /* 구글 로그인(토큰, 회원가입 여부 반환) */
    @Transactional
    public SocialLoginResult googleLogin(String code) {
        // 구글 토큰, 프로필 조회
        GoogleDTO.OAuthToken oAuthToken = googleUtil.requestToken(code);
        GoogleDTO.GoogleProfile googleProfile = googleUtil.requestProfile(oAuthToken);

        // 이메일
        String email = googleProfile.getEmail();
        Boolean emailVerified = googleProfile.getEmail_verified();

        if (email == null || Boolean.FALSE.equals(emailVerified)) {
            throw new GeneralException(ErrorStatus.SOCIAL_PROFILE_INCOMPLETE);
        }

        // 이름
        String name = googleProfile.getName();

        return processSocialLogin(email, name, LoginType.GOOGLE);
    }

    // 소셜 로그인 공통 프로세스
    private SocialLoginResult processSocialLogin(String email, String name, LoginType loginType) {
        // 기존 유저 조회
        var optionalUser = userRepository.findByEmail(email);
        boolean registered = false;
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();

            // 이미 가입된 유저지만 탈퇴 상태인 경우
            if (user.getDeletedAt() != null) {
                user.recoverAccount();
            }

            // 로그인 타입 검증
            if (user.getLoginType() != loginType) {
                throw new GeneralException(ErrorStatus.DUPLICATE_USER_REGISTERED);
            }

            // 회원가입 완료 여부 확인
            boolean alreadyCompleted =
                    designerRepository.existsByUser_Id(user.getId()) ||
                            modelRepository.existsByUser_Id(user.getId());

            registered = alreadyCompleted;
        } else {
            // 더미 User 생성
            user = User.builder()
                    .loginId(null)
                    .password(null)
                    .email(email)
                    .name(name)
                    .phoneNum(null)
                    .gender(null)
                    .birth(null)
                    .imageUrl(null)
                    .loginType(loginType)
                    .userRole(null)
                    .permission(Permission.USER)
                    .build();

            user.createNotificationSetting();

            userRepository.save(user);
        }

        // JWT 발급 및 Redis 저장
        TokenResponse tokens = tokenProvider.createToken(user);
        String refreshToken = tokens.getRefreshToken();
        long ttlSec = tokenProvider.getRemainingSeconds(refreshToken);

        String currentKey = RT_KEY_PREFIX + user.getId() + ":current";

        redisService.setRefreshToken(
                currentKey,
                refreshToken,
                ttlSec
        );

        Designer designer = null;
        if (user.getUserRole() == UserRole.DESIGNER){
            designer = designerService.getByUser(user);
        }

        // 응답 생성
        SocialLoginResponse loginResponse = registered
                ? SocialLoginResponse.existing(user.getId(), tokens.getAccessToken(), user.getUserRole(),
                user.getUserRole()==UserRole.DESIGNER? designer.getCategory().getDescription() : null)
                : SocialLoginResponse.newUser(user.getId(), tokens.getAccessToken(), user.getUserRole(),
                user.getUserRole()==UserRole.DESIGNER? designer.getCategory().getDescription() : null);

        return SocialLoginResult.of(loginResponse, refreshToken, ttlSec);
    }

    /* ---------- 탈퇴하기 ---------- */
    @Transactional
    public void withdraw(HttpServletRequest request) {
        // 1. 토큰 추출 및 검증
        String accessToken = tokenProvider.resolveToken(request);
        if (accessToken == null || accessToken.isBlank()) {
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);
        }

        // 2. 유저 ID 추출
        String userIdStr = tokenProvider.getUserIdFromToken(accessToken);
        Long userId = Long.valueOf(userIdStr);

        // 3. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        // 4. Soft Delete 처리
        if (user.getDeletedAt() != null) {
            throw new GeneralException(ErrorStatus.WITHDRAWN_USER);
        }

        redisService.deleteValue(RT_KEY_PREFIX + userId + ":current");
        redisService.deleteValue(RT_KEY_PREFIX + userId + ":previous");

        user.markAsDeleted();
    }
}

