package modelly.modelly_be.domain.auth.dto.internal;

import jakarta.validation.constraints.*;
import lombok.*;
import modelly.modelly_be.global.entity.Category;

@Getter
public class DesignerExtra {

    private String shop;

    private String addressLine1;

    private String addressLine2;

    @NotNull(message = "카테고리는 필수입니다.")
    private Category category;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

}
