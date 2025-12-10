package modelly.modelly_be.domain.review.dto.response;

import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.entity.ReviewImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ReviewResponseDto(
        Long reviewId,
        float rating,
        String content,
        List<String> imageUrls
) {

    public static ReviewResponseDto of(Review review) {

        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        return new ReviewResponseDto(
                review.getId(),
                review.getRating(),
                review.getContent(),
                imageUrls);
    }
}
