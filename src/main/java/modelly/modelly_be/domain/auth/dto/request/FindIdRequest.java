package modelly.modelly_be.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;


@Getter
public class FindIdRequest {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;
}
