package modelly.modelly_be.domain.review.dto.response;

import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.entity.ReviewImage;
import java.util.List;

public record ReviewResponseDto(
        Long reviewId,
        float rating,
        String content,
        List<String> imageUrls,
        boolean isMine
) {

    public static ReviewResponseDto of(Review review, boolean isMine) {

        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        return new ReviewResponseDto(
                review.getId(),
                review.getRating(),
                review.getContent(),
                imageUrls,
                isMine);
    }
}
