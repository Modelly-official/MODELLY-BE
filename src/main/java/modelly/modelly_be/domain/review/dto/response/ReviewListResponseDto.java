package modelly.modelly_be.domain.review.dto.response;

import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.entity.ReviewImage;

import java.time.LocalDate;
import java.util.List;

public record ReviewListResponseDto(
        Long reviewId,
        String modelImage,
        String modelname,
        float rating,
        LocalDate createdDate,
        String content,
        String summary,
        List<String> reviewImages,
        boolean isMine
) {

    public static ReviewListResponseDto of(Review review, String modelImage, boolean isMine) {

        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        LocalDate createdDate = review.getCreatedAt().toLocalDate();

        return new ReviewListResponseDto(
                review.getId(),
                modelImage,
                review.getModel().getNickname(),
                review.getRating(),
                createdDate,
                review.getContent(),
                review.getSummary(),
                imageUrls,
                isMine
        );
    }


}
