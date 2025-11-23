package modelly.modelly_be.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.auth.dto.internal.LoginResult;
import modelly.modelly_be.domain.auth.dto.internal.NewTokenResult;
import modelly.modelly_be.domain.auth.dto.request.*;
import modelly.modelly_be.domain.auth.dto.response.*;
import modelly.modelly_be.domain.auth.service.AccountRecoveryService;
import modelly.modelly_be.domain.auth.service.AuthService;
import modelly.modelly_be.domain.auth.service.SmsAuthService;
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
    private final SmsAuthService smsAuthService;
    private final AccountRecoveryService accountRecoveryService;

    @Value("${security.cookie.secure:true}")
    private boolean refreshCookieSecure;

    /* ---------- 회원가입/로그인/로그아웃 ----------*/
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
                refreshCookieSecure,
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
                refreshCookieSecure,
                ""
        );
        response.addHeader("Set-Cookie", del.toString());

        return ApiResponse.onSuccess(authService.logout(request));
    }

    /* ---------- 토큰 재발급 및 토큰 만료 확인---------- */

    /* 토큰 재발급 */
    @Operation(summary = "Access Token 재발급", description = "사용자의 Access Token과 Refresh Token(쿠키)을 재발급합니다.")
    @PostMapping("/auth/refresh")
    public ApiResponse<AccessTokenResponse> newAccessToken(
            @CookieValue(name = CookieUtil.REFRESH_COOKIE, required = false) String refreshToken, HttpServletResponse response) {

        NewTokenResult result = authService.newAccessToken(refreshToken);

        // 새 리프레시 토큰으로 쿠키 재세팅
        var cookie = CookieUtil.buildRefreshCookie(
                result.getRefreshToken(),
                result.getRefreshTtlSec(),
                "",
                refreshCookieSecure,
                ""
        );
        // Set-Cookie 헤더에 추가
        response.addHeader("Set-Cookie", cookie.toString());

        // 액세스 토큰 반환
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.of(result.getAccessTokenResponse().getAccessToken());
        return ApiResponse.onSuccess(accessTokenResponse);
    }

    /* Access Token 만료 기간 확인 */
    @Operation(summary = "Access Token 만료 확인", description = "사용자의 Access Token의 만료 여부를 확인합니다.")
    @GetMapping("/auth/validate")
    public ApiResponse<TokenValidationResponse> validate(HttpServletRequest request) {
        return ApiResponse.onSuccess(authService.isValidAccess(request));
    }

    /* ---------- 아이디/이메일 중복 체크 ---------- */

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

    /* ---------- SMS 전송 및 인증 ---------- */

    /* SMS 인증번호 전송 */
    @PostMapping("/auth/sms/send-code")
    public ApiResponse<SimpleMessageDTO> sendAuthCode(@Valid @RequestBody SmsAuthRequest request) {
        smsAuthService.sendAuthCode(request.getPhoneNumber());
        return ApiResponse.onSuccess(new SimpleMessageDTO("SMS 인증번호 발송 성공"));
    }

    /* SMS 인증 */
    @PostMapping("/auth/sms/verify")
    public ApiResponse<SimpleMessageDTO> verifyAuthCode(@Valid @RequestBody VerifySmsAuthRequest request) {
        smsAuthService.verifyAuthCode(request.getPhoneNumber(), request.getAuthCode());
        return ApiResponse.onSuccess(new SimpleMessageDTO("SMS 인증 성공"));
    }

    /* ---------- 아이디 찾기/비밀번호 재설정 ---------- */

    /* 이메일 인증번호 검증 (공통) */
    @Operation(summary = "이메일 인증번호 검증", description = "이메일로 발송된 인증번호가 일치하는지 확인합니다.")
    @PostMapping("/auth/email/verify")
    public ApiResponse<SimpleMessageDTO> verifyEmailCode(@Valid @RequestBody EmailCodeVerifyRequest request) {
        accountRecoveryService.verifyEmailCode(request);
        return ApiResponse.onSuccess(new SimpleMessageDTO("이메일 인증 성공"));
    }


    /* 아이디 찾기 - 인증번호 발송 */
    @Operation(summary = "아이디 찾기 - 인증번호 발송", description = "요청 정보에 맞는 사용자가 존재하면 해당 이메일로 인증번호를 전송합니다.")
    @PostMapping("/auth/find-id/send-code")
    public ApiResponse<SimpleMessageDTO> sendFindIdCode(@Valid @RequestBody FindIdRequest request) {
        accountRecoveryService.sendFindIdCode(request);
        return ApiResponse.onSuccess(new SimpleMessageDTO("이메일 인증번호 발송 성공"));
    }

    /* 아이디 찾기 - 이름, 아이디 반환 */
    @Operation(summary = "아이디 찾기 - 이름, 아이디 반환", description = "이메일 인증 완료한 사용자의 이름과 아이디를 반환합니다.")
    @PostMapping("/auth/find-id")
    public ApiResponse<FindIdResponse> findId(@Valid @RequestBody FindIdRequest request) {
        FindIdResponse response = accountRecoveryService.findId(request);
        return ApiResponse.onSuccess(response);
    }

    /* 비밀번호 재설정 - 인증번호 발송 */
    @Operation(summary = "비밀번호 재설정 - 인증번호 발송", description = "요청 정보에 맞는 사용자가 존재하면 해당 이메일로 인증번호를 전송합니다.")
    @PostMapping("/auth/reset-password/send-code")
    public ApiResponse<SimpleMessageDTO> sendResetPasswordCode(@Valid @RequestBody ResetPasswordRequest request) {
        accountRecoveryService.sendResetPasswordCode(request);
        return ApiResponse.onSuccess(new SimpleMessageDTO("이메일 인증번호 발송 성공"));
    }

    /* 비밀번호 재설정 - 사용자 정보 검증 */
    @Operation(summary = "비밀번호 재설정 - 사용자 정보 검증", description = "요청 정보에 맞는 사용자 존재 여부 및 이메일 인증 여부 확인하여 비밀번호 재설정 권한을 설정합니다.")
    @PostMapping("/auth/reset-password/verify")
    public ApiResponse<SimpleMessageDTO> verifyResetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        accountRecoveryService.verifyResetPassword(request);
        return ApiResponse.onSuccess(new SimpleMessageDTO("비밀번호 재설정 인증 완료"));
    }

    /* 비밀번호 재설정 - 새로운 비밀번호로 변경 */
    @Operation(summary = "비밀번호 재설정 - 새로운 비밀번호로 변경", description = "검증이 완료된 사용자의 비밀번호를 변경합니다.")
    @PostMapping("/auth/reset-password")
    public ApiResponse<SimpleMessageDTO> resetPassword(@Valid @RequestBody ResetPasswordConfirmRequest request) {
        accountRecoveryService.resetPassword(request);
        return ApiResponse.onSuccess(new SimpleMessageDTO("비밀번호 재설정 완료"));
    }
}
