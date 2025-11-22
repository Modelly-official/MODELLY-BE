package modelly.modelly_be.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import modelly.modelly_be.domain.auth.dto.internal.DesignerExtra;
import modelly.modelly_be.domain.auth.dto.internal.LoginResult;
import modelly.modelly_be.domain.auth.dto.internal.ModelExtra;
import modelly.modelly_be.domain.auth.dto.request.LoginRequest;
import modelly.modelly_be.domain.auth.dto.request.SignupRequest;
import modelly.modelly_be.domain.auth.dto.response.*;
import modelly.modelly_be.domain.user.entity.*;
import modelly.modelly_be.domain.user.repository.ModelRepository;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.security.entity.TokenStatus;
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
                .userRole(UserRole.USER)
                .build();

        userRepository.save(user);

        // Designer, Model 정보 받아옴
        DesignerExtra designerExtra = req.getDesigner();
        ModelExtra modelExtra = req.getModel();
        String nickname;

        // Designer, Model 정보가 둘 다 존재하는 경우
        if (designerExtra != null && modelExtra != null) {
            System.out.printf("asdf");
            throw new GeneralException(ErrorStatus.SIGNUP_FIELDS_ERROR);
        }

        // Designer 회원가입 정보
        if (designerExtra != null) {
            Designer designer = Designer.builder()
                    .user(user)
                    .shop(designerExtra.getShop())
                    .shopAddress(designerExtra.getShopAddress())
                    .category(designerExtra.getCategory())
                    .chemistryScore(0L)
                    .nickname(designerExtra.getNickname())
                    .build();

            designerRepository.save(designer);
            nickname = designer.getNickname();
        }
        else if (modelExtra != null) { // Model 회원가입 정보
            Model model = Model.of(user, modelExtra.getNickname());
            modelRepository.save(model);
            nickname = model.getNickname();
        } else {
            // Designer, Model 정보 둘 다 존재하지 않는 경우
            throw new GeneralException(ErrorStatus.SIGNUP_FIELDS_ERROR);
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

    @Transactional
    public SimpleMessageDTO logout(HttpServletRequest request) {
        String accessToken = tokenProvider.resolveToken(request);

        if (accessToken == null || accessToken.isBlank())
            throw new GeneralException(ErrorStatus.TOKEN_INVALID);

        String userId = tokenProvider.getUserIdFromToken(accessToken);
        redisService.deleteValue(RT_KEY_PREFIX + userId);

        return new SimpleMessageDTO("로그아웃이 완료되었습니다.");
    }


    @Transactional(readOnly = true)
    public AccessTokenResponse newAccessToken(String refreshToken) { // 프론트에서 Authorization 헤더를 빼고 전송해줘야함.

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

        // AccessToken만 새로 발급
        String newAccess = tokenProvider.createAccessToken(user);

        return AccessTokenResponse.of(newAccess);
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

    // util
    @Transactional(readOnly = true)
    public TokenValidationResponse isValidAccess(HttpServletRequest request) {
        // 유효성 검사는 JwtAuthenticationFilter에서 처리
        String accessToken = tokenProvider.resolveToken(request);
        TokenStatus status = tokenProvider.validateToken(accessToken);

        return TokenValidationResponse.of("Access Token이 유효합니다.", status);
    }
}

