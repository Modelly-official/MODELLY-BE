package modelly.modelly_be.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import modelly.modelly_be.domain.auth.dto.internal.*;
import modelly.modelly_be.domain.auth.dto.request.SocialSignupRequest;
import modelly.modelly_be.domain.auth.dto.request.LoginRequest;
import modelly.modelly_be.domain.auth.dto.request.SignupRequest;
import modelly.modelly_be.domain.auth.dto.response.*;
import modelly.modelly_be.domain.user.entity.*;
import modelly.modelly_be.domain.user.repository.ModelRepository;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.security.entity.TokenStatus;
import modelly.modelly_be.global.security.kakao.KakaoDTO;
import modelly.modelly_be.global.security.kakao.KakaoUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import modelly.modelly_be.domain.user.repository.DesignerRepository;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.redis.RedisService;
import modelly.modelly_be.global.security.dto.TokenResponse;
import modelly.modelly_be.global.security.jwt.TokenProvider;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String RT_KEY_PREFIX = "refresh-token:";

    private final UserRepository userRepository;
    private final DesignerRepository designerRepository;
    private final ModelRepository modelRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final KakaoUtil kakaoUtil;

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

            Designer designer = Designer.builder()
                    .user(user)
                    .shop(designerExtra.getShop())
                    .shopAddress(designerExtra.getShopAddress())
                    .category(designerExtra.getCategory())
                    .chemistryScore(0L)
                    .nickname(designerExtra.getNickname())
                    .instagramId(null)
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

        return SignupResponse.of("회원가입이 완료되었습니다.", user.getLoginId(), user.getName(), nickname);
    }

    /* 로그인 */
    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest req) {
        User user = userRepository.findByLoginId(req.getLoginId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.LOGIN_FAIL));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new GeneralException(ErrorStatus.LOGIN_FAIL);

        TokenResponse tokens = tokenProvider.createToken(user);

        // refresh token
        String refreshToken = tokens.getRefreshToken();

        // userId 기준으로 레디스에 refresh token 저장(TTL=refresh 남은 시간)
        long ttlSec = tokenProvider.getRemainingSeconds(tokens.getRefreshToken());
        redisService.setRefreshToken(RT_KEY_PREFIX + user.getId(), tokens.getRefreshToken(), ttlSec);

        // 로그인 응답 생성
        LoginResponse loginResponse = LoginResponse.of(
                user.getId(),
                tokens.getAccessToken()
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
        redisService.deleteValue(RT_KEY_PREFIX + userId);

        return new SimpleMessageDTO("로그아웃이 완료되었습니다.");
    }


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
        String key = RT_KEY_PREFIX + userId;
        String stored = redisService.getValue(key);
        if (stored == null || stored.isEmpty()) {
            // 만료/로그아웃 등으로 없는 상태
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }
        if (!stored.equals(refreshToken)) {
            // 탈취 등으로 일치하지 않는 상태
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);
        }

        // user 조회
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        //  Access, Refresh 발급
        String newAccess = tokenProvider.createAccessToken(user);
        String newRefresh = tokenProvider.createRefreshToken(user);

        // Redis에 새 refresh 저장
        long ttlSec = tokenProvider.getRemainingSeconds(newRefresh);
        redisService.setRefreshToken(key, newRefresh, ttlSec);


        return NewTokenResult.of(AccessTokenResponse.of(newAccess), newRefresh, ttlSec);
    }



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

    // Access Token 유효한지 확인
    @Transactional(readOnly = true)
    public TokenValidationResponse isValidAccess(HttpServletRequest request) {
        // 유효성 검사는 JwtAuthenticationFilter에서 처리
        String accessToken = tokenProvider.resolveToken(request);

        if (accessToken == null || accessToken.isBlank())
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);

        TokenStatus status = tokenProvider.validateToken(accessToken);

        return TokenValidationResponse.of("Access Token의 상태는 다음과 같습니다.", status);
    }

    /* 소셜 회원가입 */
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

        // 5) Designer / Model 정보 저장
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

            Designer designer = Designer.builder()
                    .user(user)
                    .shop(designerExtra.getShop())
                    .shopAddress(designerExtra.getShopAddress())
                    .category(designerExtra.getCategory())
                    .chemistryScore(0L)
                    .nickname(designerExtra.getNickname())
                    .instagramId(null)
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

        return SignupResponse.of("회원가입이 완료되었습니다.", user.getEmail(), user.getName(), nickname);
    }

    /* 카카오 로그인(토큰, 회원가입 여부 반환) */
    @Transactional
    public SocialLoginResult kakaoLogin(String code, String redirectUri) {
        // 카카오 토큰으로 프로필 조회
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(code, redirectUri);
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        var kakaoAccount = kakaoProfile.getKakao_account();
        if (kakaoAccount == null || kakaoAccount.getEmail() == null) {
            throw new GeneralException(ErrorStatus.SOCIAL_PROFILE_INCOMPLETE);
        }

        // 카카오로부터 이메일, 이름 정보 받아오기
        String kakaoEmail = kakaoAccount.getEmail();
        String kakaoName = kakaoProfile.getKakao_account().getProfile().getNickname();

        // 기존 사용자인지 파악
        var optionalUser = userRepository.findByEmail(kakaoEmail);
        boolean registered = false;
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();

            // 카카오 로그인으로 가입한 유저가 아닌 경우
            if (user.getLoginType() != LoginType.KAKAO) {
                throw new GeneralException(ErrorStatus.DUPLICATE_USER_REGISTERED);
            }

            // 회원가입 완료 여부 확인(더미 유저인지 아닌지)
            boolean alreadyCompleted =
                    designerRepository.existsByUser_Id(user.getId()) ||
                    modelRepository.existsByUser_Id(user.getId());

            registered = alreadyCompleted;
        } else {
            // 회원가입하지 않은 경우, 더미 User 생성

            user = User.builder()
                    .loginId(null)
                    .password(null)
                    .email(kakaoEmail)
                    .name(kakaoName)
                    .phoneNum(null)
                    .gender(null)
                    .birth(null)
                    .imageUrl(null)
                    .loginType(LoginType.KAKAO)
                    .userRole(null)
                    .permission(Permission.USER)
                    .build();

            userRepository.save(user);
        }

        // JWT 발급
        TokenResponse tokens = tokenProvider.createToken(user);

        // Redis에 refresh token 저장
        String refreshToken = tokens.getRefreshToken();
        long ttlSec = tokenProvider.getRemainingSeconds(refreshToken);
        redisService.setRefreshToken(RT_KEY_PREFIX + user.getId(), refreshToken, ttlSec);

        // 응답(액세스 토큰, 회원가입 여부)
        SocialLoginResponse loginResponse = registered
                ? SocialLoginResponse.existing(user.getId(), tokens.getAccessToken()) // 기존 회원
                : SocialLoginResponse.newUser(user.getId(), tokens.getAccessToken()); // 신규 회원

        return SocialLoginResult.of(loginResponse, refreshToken, ttlSec);
    }
}

