package modelly.modelly_be.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReviewThumbnailListResponseDto(
        @Schema(description = "리뷰 id", example="1")
        Long reviewId,
        @Schema(description = "리뷰 썸네일")
        String reviewThumbnail,
        @Schema(description = "리뷰 고정 여부", example="false")
        boolean isFixed
) {
}
