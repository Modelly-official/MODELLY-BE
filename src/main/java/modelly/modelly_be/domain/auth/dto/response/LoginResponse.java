package modelly.modelly_be.domain.auth.dto.response;

import lombok.*;
import modelly.modelly_be.domain.user.entity.Role;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponse {
    private Long userId;
    private Role role;
    private String accessToken;
    private String refreshToken;

    public static LoginResponse of (Long userId, Role role, String accessToken, String refreshToken) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.userId = userId;
        loginResponse.role = role;
        loginResponse.accessToken = accessToken;
        loginResponse.refreshToken = refreshToken;

        return loginResponse;
    }
}
