package modelly.modelly_be.domain.auth.dto.internal;

import jakarta.validation.constraints.*;
import lombok.*;
import modelly.modelly_be.global.entity.Category;

@Getter
public class DesignerExtra {

    @NotNull(message = "샵 이름은 필수입니다.")
    private String shop;

    @NotNull(message = "기본 주소는 필수입니다.")
    private String addressLine1;

    @NotNull(message = "상세 주소는 필수입니다.")
    private String addressLine2;

    @NotNull(message = "카테고리는 필수입니다.")
    private Category category;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    @NotBlank(message = "한줄 소개는 필수 입력 값입니다.")
    private String intro;

}
