package modelly.modelly_be.domain.review.dto.response;

import modelly.modelly_be.domain.review.dto.common.ReplyDto;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.entity.ReviewImage;

import java.time.LocalDate;
import java.util.List;

public record ReviewListResponseDto(
        Long reviewId,
        String modelImage,
        String modelName,
        float rating,
        LocalDate createdDate,
        String content,
        String summary,
        List<String> reviewImages,
        boolean isFixed,
        boolean isMine,
        ReplyDto replyDto
) {

    public static ReviewListResponseDto of(Review review, String modelImage, boolean isMine, Reply reply) {

        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        LocalDate createdDate = review.getCreatedAt().toLocalDate();

        ReplyDto replyDto = ReplyDto.of(reply);

        return new ReviewListResponseDto(
                review.getId(),
                modelImage,
                review.getModel().getNickname(),
                review.getRating(),
                createdDate,
                review.getContent(),
                review.getSummary(),
                imageUrls,
                review.isFixed(),
                isMine,
                replyDto
        );
    }

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
                review.isFixed(),
                isMine,
                null
        );
    }


}
