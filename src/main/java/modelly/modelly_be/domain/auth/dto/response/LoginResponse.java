package modelly.modelly_be.domain.auth.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponse {
    private Long userId;
    private String accessToken;

    public static LoginResponse of (Long userId, String accessToken) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.userId = userId;
        loginResponse.accessToken = accessToken;

        return loginResponse;
    }
}
