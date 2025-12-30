package modelly.modelly_be.domain.auth.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponse {
    private Long userId;
    private String accessToken;
    private String userRole;
    private String category;

    public static LoginResponse of (Long userId, String accessToken, String userRole, String category) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.userId = userId;
        loginResponse.accessToken = accessToken;
        loginResponse.userRole = userRole;
        loginResponse.category = category;

        return loginResponse;
    }
}
