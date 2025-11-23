package modelly.modelly_be.domain.auth.dto.internal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.auth.dto.response.SocialLoginResponse;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialLoginResult {
    private SocialLoginResponse loginResponse;
    private String refreshToken;
    private long refreshTtlSec;

    public static SocialLoginResult of(SocialLoginResponse loginResponse, String refreshToken, long refreshTtlSec) {
        SocialLoginResult result = new SocialLoginResult();
        result.loginResponse = loginResponse;
        result.refreshToken = refreshToken;
        result.refreshTtlSec = refreshTtlSec;
        return result;
    }
}
