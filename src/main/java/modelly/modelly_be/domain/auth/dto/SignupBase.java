package modelly.modelly_be.domain.auth.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.user.entity.Gender;
import modelly.modelly_be.domain.user.entity.Role;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SignupBase {
    @NotBlank
    private String loginId;

    @NotBlank
    private String password;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String phoneNum;

    @NotNull
    private Gender gender;

    @NotNull
    private LocalDate birth;

    @NotBlank
    private String nickname;

    @NotBlank
    private String imageUrl;

    @NotNull
    private Role role; // MODEL or DESIGNER
}
