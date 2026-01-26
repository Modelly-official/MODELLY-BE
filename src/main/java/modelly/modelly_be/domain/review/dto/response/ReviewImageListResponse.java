package modelly.modelly_be.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReviewImageListResponse(
        @Schema(description = "리뷰 이미지 id", example="1")
        Long reviewImageId,
        @Schema(description = "리뷰 id", example="1")
        Long reviewId,
        @Schema(description = "리뷰 이미지")
        String reviewImage,
        @Schema(description = "리뷰 고정 여부", example="false")
        boolean isFixed
) {
}
