package modelly.modelly_be.domain.auth.dto.internal;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ModelExtra {

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;
}
