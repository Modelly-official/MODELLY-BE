package modelly.modelly_be.domain.auth.dto.internal;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.auth.dto.response.LoginResponse;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResult {
    // login 컨트롤러에서 사용할 정보를 전달하는 DTO
    private LoginResponse loginResponse;
    private String refreshToken;
    private long refreshTtlSec;

    public static LoginResult of(LoginResponse loginResponse, String refreshToken, long refreshTtlSec) {
        LoginResult result = new LoginResult();
        result.loginResponse = loginResponse;
        result.refreshToken = refreshToken;
        result.refreshTtlSec = refreshTtlSec;
        return result;
    }
}
