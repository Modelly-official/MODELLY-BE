package modelly.modelly_be.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.entity.ReviewImage;
import java.util.List;

public record ReviewResponseDto(
        @Schema(description = "리뷰 id", example="1")
        Long reviewId,
        @Schema(description = "별점", example="5.0")
        float rating,
        @Schema(description = "리뷰 내용", example="머리를 너무 잘해주세요 감동입니다 흑흑")
        String content,
        @Schema(description = "시술 내용", example="커트, 파마")
        String summary,
        @Schema(description = "리뷰 이미지 리스트")
        List<String> imageUrls,
        @Schema(description = "작성자인지 여부", example="true")
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
                review.getSummary(),
                imageUrls,
                isMine);
    }
}
