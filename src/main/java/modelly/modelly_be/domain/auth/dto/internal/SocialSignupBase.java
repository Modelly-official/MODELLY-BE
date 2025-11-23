package modelly.modelly_be.domain.auth.dto.internal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.user.entity.Gender;
import modelly.modelly_be.domain.user.entity.UserRole;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SocialSignupBase {

    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    private String phoneNum;

    @NotNull(message = "성별은 필수 입력 값입니다.")
    private Gender gender;

    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    private LocalDate birth;

    @NotNull(message = "유저 역할은 필수 입력 값입니다.")
    private UserRole userRole;

    private String imageUrl;

}
