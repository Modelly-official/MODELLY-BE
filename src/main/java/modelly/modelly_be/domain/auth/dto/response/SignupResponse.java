package modelly.modelly_be.domain.auth.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupResponse {
    private String message;
    private String loginId;
    private String name;
    private String nickname;
    private String userRole;

    public static SignupResponse of(String message, String loginId, String name, String nickname, String userRole) {
        SignupResponse signupResponse = new SignupResponse();
        signupResponse.message = message;
        signupResponse.loginId = loginId;
        signupResponse.name = name;
        signupResponse.nickname = nickname;
        signupResponse.userRole = userRole;
        return signupResponse;
    }
}
