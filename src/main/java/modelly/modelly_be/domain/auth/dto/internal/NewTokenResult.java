package modelly.modelly_be.domain.auth.dto.internal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.auth.dto.response.AccessTokenResponse;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewTokenResult {
    private AccessTokenResponse accessTokenResponse;
    private String refreshToken;
    private long refreshTtlSec;

    public static NewTokenResult of(AccessTokenResponse accessTokenResponse, String refreshToken, long refreshTtlSec) {
        NewTokenResult result = new NewTokenResult();
        result.accessTokenResponse = accessTokenResponse;
        result.refreshToken = refreshToken;
        result.refreshTtlSec = refreshTtlSec;
        return result;
    }
}
