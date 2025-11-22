package modelly.modelly_be.domain.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import modelly.modelly_be.domain.infra.mail.EmailAuthType;

@Getter
public class EmailCodeVerifyRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String authCode;

    @NotNull
    private EmailAuthType type; // FIND_ID, RESET_PASSWORD
}
