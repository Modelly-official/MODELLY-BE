package modelly.modelly_be.domain.auth.dto.internal;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.user.entity.Gender;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SignupBase {
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    private String phoneNum;

    @NotNull(message = "성별은 필수 입력 값입니다.")
    private Gender gender;

    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    private LocalDate birth;

    private String imageUrl;

}
