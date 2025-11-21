package modelly.modelly_be.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.auth.dto.internal.LoginResult;
import modelly.modelly_be.domain.auth.dto.request.*;
import modelly.modelly_be.domain.auth.dto.response.*;
import modelly.modelly_be.domain.auth.service.AuthService;
import modelly.modelly_be.domain.auth.service.PhoneAuthService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.apiPayload.code.status.SuccessStatus;
import modelly.modelly_be.global.security.jwt.CookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PhoneAuthService phoneAuthService;

    @Value("${security.cookie.secure:true}")
    private boolean refreshCookieSecure;

    @Operation(summary = "회원가입", description = "회원가입 완료 메시지, loginId, 이름, 닉네임을 반환합니다.")
    @PostMapping("/auth/signup")
    public ApiResponse<SignupResponse> signup(@RequestBody @Valid SignupRequest request) {
        return ApiResponse.of(SuccessStatus.CREATED,authService.signup(request));
    }

    /* 로그인 */
    @Operation(summary = "로그인", description = "Access/Refresh 토큰을 포함한 로그인 응답을 반환합니다.")
    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        LoginResult result = authService.login(request);

        var cookie = CookieUtil.buildRefreshCookie(
                result.getRefreshToken(),
                result.getRefreshTtlSec(),
                "",
                true,
                ""
        );
        response.addHeader("Set-Cookie", cookie.toString());

        return ApiResponse.onSuccess(result.getLoginResponse());
    }

    /* 로그아웃 */
    @Operation(summary = "로그아웃", description = "사용자의 Refresh Token을 Redis에서 삭제합니다.")
    @PostMapping("/auth/logout")
    public ApiResponse<SimpleMessageDTO> logout(HttpServletRequest request, HttpServletResponse response) {
        var del = CookieUtil.deleteRefreshCookie(
                "",
                true,
                ""
        );
        response.addHeader("Set-Cookie", del.toString());

        return ApiResponse.onSuccess(authService.logout(request));
    }

    /* 토큰 재발급 */
    @Operation(summary = "Access Token 재발급", description = "사용자의 Access Token만 재발급합니다.")
    @PostMapping("/auth/refresh")
    public ApiResponse<AccessTokenResponse> newAccessToken(@CookieValue(name = CookieUtil.REFRESH_COOKIE, required = false) String refreshToken) {
        return ApiResponse.onSuccess(authService.newAccessToken(refreshToken));
    }

    /* Access Token 만료 기간 확인 */
    @Operation(summary = "Access Token 만료 확인", description = "사용자의 Access Token의 만료 여부를 확인합니다.")
    @GetMapping("/auth/validate")
    public ApiResponse<TokenValidationResponse> validate(HttpServletRequest request) {
        return ApiResponse.onSuccess(authService.isValidAccess(request));
    }

    /* 로그인 아이디 중복 체크 */
    @Operation(summary = "아이디 중복 체크", description = "available = true면 중복 X")
    @GetMapping("/check/login-id")
    public ApiResponse<DuplicateCheckResponse> checkLoginId(@RequestParam("value") String value) {
        return ApiResponse.onSuccess(authService.checkLoginId(value));
    }

    /* 이메일 중복 체크 */
    @Operation(summary = "이메일 중복 체크", description = "available = true면 중복 X")
    @GetMapping("/check/email")
    public ApiResponse<DuplicateCheckResponse> checkEmail(@RequestParam("value") String value) {
        return ApiResponse.onSuccess(authService.checkEmail(value));
    }

    /* SMS 인증번호 전송 */
    @PostMapping("/auth/phone/send")
    public ApiResponse<SimpleMessageDTO> sendAuthCode(@Valid @RequestBody PhoneAuthRequest request) {
        phoneAuthService.sendAuthCode(request.getPhoneNumber());
        return ApiResponse.onSuccess(new SimpleMessageDTO("인증번호 발송 성공"));
    }

    /* SMS 인증 */
    @PostMapping("/auth/phone/verify")
    public ApiResponse<SimpleMessageDTO> verifyAuthCode(@Valid @RequestBody VerifyPhoneAuthRequest request) {
        phoneAuthService.verifyAuthCode(request.getPhoneNumber(), request.getAuthCode());
        return ApiResponse.onSuccess(new SimpleMessageDTO("인증 성공"));
    }
}
