package modelly.modelly_be.domain.review.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewCreateRequestDto(
        @NotNull(message = "별점은 필수입니다.")
        @DecimalMin(value = "0.0", message = "rating 은 0.0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "rating 은 5.0 이하여야 합니다.")
        Float rating,
        @NotNull
        @Size(min = 10, max = 1000, message = "content 는 10자 이상, 1000자 이하여야 합니다.")
        String content,
        @Size(max = 3, message = "이미지는 최대 3장까지 업로드 가능합니다.")
        List<String> imageUrlList
) {
}
