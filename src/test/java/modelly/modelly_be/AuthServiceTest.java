package modelly.modelly_be;

import jakarta.servlet.http.HttpServletRequest;
import modelly.modelly_be.domain.auth.dto.SignupBase;
import modelly.modelly_be.domain.auth.dto.request.LoginRequest;
import modelly.modelly_be.domain.auth.dto.request.SignupRequest;
import modelly.modelly_be.domain.auth.dto.response.LoginResponse;
import modelly.modelly_be.domain.auth.service.AuthService;
import modelly.modelly_be.domain.user.entity.Gender;
import modelly.modelly_be.domain.user.entity.Role;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.UserRole;
import modelly.modelly_be.domain.user.repository.DesignerRepository;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.global.apiPayload.code.BaseErrorCode;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.redis.RedisService;
import modelly.modelly_be.global.security.dto.TokenResponse;
import modelly.modelly_be.global.security.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock DesignerRepository designerRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenProvider tokenProvider;
    @Mock RedisService redisService;

    @InjectMocks
    AuthService authService;

    // 공통 fixture
    private User user;
    private final Long userId = 100L;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .loginId("test01")
                .password("ENCODED")
                .email("t1@test.com")
                .name("테스터")
                .phoneNum("010")
                .gender(Gender.MALE)
                .birth(LocalDate.of(2000, 1, 1))
                .nickname("nick")
                .imageUrl(null)
                .role(Role.MODEL)
                .userRole(UserRole.USER)
                .build();
    }

    // 1) 회원가입 성공 (SimpleMessage 반환)
    @Test
    void signup_success_returns_simple_message() {
        // given
        SignupBase base = new SignupBase(
                "newuser",           // loginId
                "rawpw",             // password
                "n@u.com",           // email
                "뉴유저",             // name
                "010",               // phoneNum
                Gender.MALE,                // gender
                LocalDate.of(2000, 1, 1),        // birth
                "nick",              // nickname
                null,                // imageUrl
                Role.MODEL       // role
        );

        SignupRequest req = new SignupRequest(base, null);

        when(userRepository.existsByLoginId("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("n@u.com")).thenReturn(false);
        when(passwordEncoder.encode("rawpw")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            // save되며 id 부여된다고 가정
            return User.builder()
                    .id(555L)
                    .loginId(u.getLoginId())
                    .password(u.getPassword())
                    .email(u.getEmail())
                    .name(u.getName())
                    .role(u.getRole())
                    .userRole(u.getUserRole())
                    .build();
        });

        // when
        SimpleMessageDTO msg = authService.signup(req);

        // then
        assertThat(msg.getMessage()).isEqualTo("회원가입이 완료되었습니다.");
        verify(userRepository).save(any(User.class));
        verifyNoInteractions(designerRepository, tokenProvider, redisService);
    }

    // 2) 로그인 성공: 토큰 발급 + Redis 저장 (TTL=getRemainingSeconds)
    @Test
    void login_success_stores_refresh_in_redis_with_ttl() {
        // given
        LoginRequest req = new LoginRequest("test01", "pw");

        when(userRepository.findByLoginId("test01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pw", "ENCODED")).thenReturn(true);

        when(tokenProvider.createToken(any(User.class)))
                .thenReturn(TokenResponse.of("ACCESS.JWT", "REFRESH.JWT"));
        when(tokenProvider.getRemainingSeconds("REFRESH.JWT")).thenReturn(1234L);

        // when
        LoginResponse resp = authService.login(req);

        // then
        assertThat(resp.getUserId()).isEqualTo(userId);
        assertThat(resp.getAccessToken()).isEqualTo("ACCESS.JWT");
        assertThat(resp.getRefreshToken()).isEqualTo("REFRESH.JWT");

        verify(redisService).setRefreshToken("refresh-token:" + userId, "REFRESH.JWT", 1234L);
    }

    // 3) 로그아웃: 요청에서 access 추출 → refresh 삭제, 메시지 반환
    @Test
    void logout_deletes_refresh_and_returns_message() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);

        // 토큰 추출 스텁
        when(tokenProvider.resolveToken(request)).thenReturn("ACCESS.JWT");

        // userId 추출 스텁
        when(tokenProvider.getUserIdFromToken("ACCESS.JWT")).thenReturn(String.valueOf(userId));

        // when
        SimpleMessageDTO msg = authService.logout(request);

        // then
        assertThat(msg.getMessage()).contains("로그아웃");
        verify(redisService).deleteValue("refresh-token:" + userId);
    }

    // 4) 재발급 성공: refresh 유효 + Redis 일치 → 새 access 발급
    @Test
    void newAccessToken_success_issues_new_access() {
        // given
        String refresh = "REFRESH.JWT";
        doReturn(true).when(tokenProvider).validateToken(eq(refresh), eq(true));
        when(tokenProvider.getUserIdFromToken(refresh)).thenReturn(String.valueOf(userId));

        when(redisService.getValue("refresh-token:" + userId)).thenReturn(refresh);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenProvider.createAccessToken(user)).thenReturn("ACCESS.NEW");

        // when
        TokenResponse tr = authService.newAccessToken(refresh);

        // then
        assertThat(tr.getAccessToken()).isEqualTo("ACCESS.NEW");
        assertThat(tr.getRefreshToken()).isEqualTo(refresh);
    }

    // 5) 재발급 실패: Redis 키 없음 → REFRESH_TOKEN_EXPIRED
    @Test
    void newAccessToken_fails_when_redis_missing() {
        String refresh = "REFRESH.JWT";
        doReturn(true).when(tokenProvider).validateToken(eq(refresh), eq(true));
        when(tokenProvider.getUserIdFromToken(refresh)).thenReturn(String.valueOf(userId));
        when(redisService.getValue("refresh-token:" + userId)).thenReturn("");

        assertThatThrownBy(() -> authService.newAccessToken(refresh))
                .isInstanceOf(GeneralException.class)
                .hasFieldOrPropertyWithValue("code", ErrorStatus.REFRESH_TOKEN_EXPIRED);
    }

    // 6) 재발급 실패: Redis 값 불일치 → TOKEN_INVALID
    @Test
    void newAccessToken_fails_when_refresh_mismatch() {
        String refresh = "REFRESH.JWT";
        doReturn(true).when(tokenProvider).validateToken(eq(refresh), eq(true));
        when(tokenProvider.getUserIdFromToken(refresh)).thenReturn(String.valueOf(userId));
        when(redisService.getValue("refresh-token:" + userId)).thenReturn("OTHER.JWT");

        assertThatThrownBy(() -> authService.newAccessToken(refresh))
                .isInstanceOf(GeneralException.class)
                .hasFieldOrPropertyWithValue("code", ErrorStatus.TOKEN_INVALID);
    }

    // 7) access 만료 확인: validate에서 만료 예외 던지면 그대로 전파되는지
    @Test
    void assertValidAccess_propagates_access_expired() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(tokenProvider.resolveToken(request)).thenReturn("ACCESS.JWT");
        doThrow(new GeneralException(ErrorStatus.ACCESS_TOKEN_EXPIRED))
                .when(tokenProvider).validateToken("ACCESS.JWT", false);

        assertThatThrownBy(() -> authService.isValidAccess(request))
                .isInstanceOf(GeneralException.class)
                .hasFieldOrPropertyWithValue("code", ErrorStatus.ACCESS_TOKEN_EXPIRED);
    }
}
