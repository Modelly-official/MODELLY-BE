package modelly.modelly_be.domain.review.dto.response;

public record ReviewThumbnailListResponseDto(
        Long reviewId,
        String reviewThumbnail,
        boolean isFixed
) {
}
