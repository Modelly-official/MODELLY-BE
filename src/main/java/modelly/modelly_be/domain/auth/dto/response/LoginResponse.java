package modelly.modelly_be.domain.auth.dto.response;

import lombok.*;
import modelly.modelly_be.domain.user.entity.Role;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponse {
    private Long userId;
    private Role role;
    private String accessToken;

    public static LoginResponse of (Long userId, Role role, String accessToken) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.userId = userId;
        loginResponse.role = role;
        loginResponse.accessToken = accessToken;

        return loginResponse;
    }
}
