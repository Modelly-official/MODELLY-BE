package modelly.modelly_be.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record MyReviewListResponseDto(
        @Schema(description = "리뷰 id", example="1")
        Long reviewId,
        @Schema(description = "예약 id", example="1")
        Long reservationId,
        @Schema(description = "디자이너 이름", example="여노")
        String designerName,
        @Schema(description = "샵 이름", example="여노살롱")
        String shop,
        @Schema(description = "샵 주소", example="서울시 마포구 상수동")
        String shopAddress,
        @Schema(description = "세부 카테고리 리스트")
        String summary,
        @Schema(description = "별점", example="5.0")
        float rating,
        @Schema(description = "리뷰 썸네일")
        String thumbnail,
        @Schema(description = "리뷰 사진 리스트")
        List<String> imageList,
        @Schema(description = "리뷰 내용", example="멋진 리뷰 감사합니다. 최고최고")
        String content,
        @Schema(description = "작성일시")
        LocalDateTime createdAt
) {

        public MyReviewListResponseDto withImages(List<String> images) {
                return new MyReviewListResponseDto(
                        reviewId, reservationId, designerName, shop, shopAddress, summary,
                        rating, thumbnail, images, content, createdAt
                );
        }

}
