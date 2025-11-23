package modelly.modelly_be.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.user.entity.LoginType;

@Getter
@AllArgsConstructor
public class FindIdResponse {
    private LoginType loginType;
    private String name;
    private String loginId;
    private String email;
}
