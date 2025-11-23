package modelly.modelly_be.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String loginId;

    @Email
    @NotBlank
    private String email;

}
