package modelly.modelly_be.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialTokenRequest {

    @NotBlank
    private String code;

    private String state;    // naver만 사용

}
