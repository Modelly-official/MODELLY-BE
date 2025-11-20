package modelly.modelly_be.domain.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import modelly.modelly_be.domain.user.entity.Category;

@Getter
@Setter
public class DesignerExtra {

    @NotBlank(message = "샵 이름은 필수입니다.")
    private String shop;

    @NotBlank(message = "샵 주소는 필수입니다.")
    private String shopAddress;

    @NotNull(message = "카테고리는 필수입니다.")
    private Category category;
}
