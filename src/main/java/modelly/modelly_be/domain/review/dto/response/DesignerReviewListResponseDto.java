package modelly.modelly_be.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import modelly.modelly_be.domain.review.dto.common.ReplyDto;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.entity.ReviewImage;

import java.time.LocalDate;
import java.util.List;

public record DesignerReviewListResponseDto(
        @Schema(description = "리뷰 id", example="1")
        Long reviewId,
        @Schema(description = "모델 프로필 사진")
        String modelImage,
        @Schema(description = "모델 이름", example="연호")
        String modelName,
        @Schema(description = "리뷰 별점", example="5.0")
        float rating,
        @Schema(description = "작성 날짜", example="2025-12-16")
        LocalDate createdDate,
        @Schema(description = "리뷰 내용", example="머리 너무 잘해주세용")
        String content,
        @Schema(description = "시술 내용", example="커트, 파마")
        String summary,
        @Schema(description = "리뷰 사진 리스트")
        List<String> reviewImages,
        @Schema(description = "리뷰 고정 여부", example="false")
        boolean isFixed,
        @Schema(description = "답글 정보")
        ReplyDto replyDto
) {

    public static DesignerReviewListResponseDto of(Review review, String modelImage, Reply reply) {

        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        LocalDate createdDate = review.getCreatedAt().toLocalDate();

        ReplyDto replyDto = ReplyDto.of(reply);

        return new DesignerReviewListResponseDto(
                review.getId(),
                modelImage,
                review.getModel().getNickname(),
                review.getRating(),
                createdDate,
                review.getContent(),
                review.getSummary(),
                imageUrls,
                review.isFixed(),
                replyDto
        );
    }

    public static DesignerReviewListResponseDto of(Review review, String modelImage) {

        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        LocalDate createdDate = review.getCreatedAt().toLocalDate();

        return new DesignerReviewListResponseDto(
                review.getId(),
                modelImage,
                review.getModel().getNickname(),
                review.getRating(),
                createdDate,
                review.getContent(),
                review.getSummary(),
                imageUrls,
                review.isFixed(),
                null
        );
    }


}
