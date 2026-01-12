package modelly.modelly_be.domain.review.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.review.dto.request.ReviewCreateRequestDto;
import modelly.modelly_be.domain.review.dto.request.ReviewUpdateRequestDto;
import modelly.modelly_be.domain.review.dto.response.MyReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "모델뷰에서의 리뷰 관련 API", description = "리뷰 작성 / 수정 / 삭제, 내가 작성한 리뷰 조회")
public interface ModelReviewSwagger {

    @Operation(summary = "리뷰 작성 API", description = """
            모델이 리뷰를 작성할 때 사용하는 API입니다. \n
            `rating`: 별점 (ex. 5.0, 3.5 ...) \n
            `content`: 리뷰 내용으로, 10자 이상 1000자 이하여야합니다. \n
            `thumbnail`: 썸네일 이미지로, 리뷰 이미지를 업로드할 경우 리뷰 이미지용 presignedURL 발급 api의 thumbnailUrl값을 넣어주시면 됩니다.  \n
            `imageUrlList`: 리뷰 사진으로 최대 3장까지만 가능합니다. \n
            `imageFolderId`: 리뷰 사진이 저장된 폴더 id로, presignedUrl 발급 후, 응답 dto의 folderId을 넣어주세요. \n
            """)
    ApiResponse<ReviewResponseDto> createReview(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long reservationId,
            @RequestBody @Valid ReviewCreateRequestDto reviewCreateRequestDto);

    @Operation(summary = "리뷰 수정 API", description = """
            모델이 리뷰를 수정할 때 사용하는 API입니다. \n
            `rating`: 별점 (ex. 5.0, 3.5 ...) \n
            `content`: 리뷰 내용으로, 10자 이상 1000자 이하여야합니다. \n
            `thumbnail`: 썸네일 이미지로, 리뷰 이미지를 업로드할 경우 리뷰 이미지용 presignedURL 발급 api의 thumbnailUrl값을 넣어주시면 됩니다.  \n
            `imageUrlList`: 리뷰 사진으로 최대 3장까지만 가능합니다. \n
            `imageFolderId`: 리뷰 사진이 저장된 폴더 id로, presignedUrl 발급 후, 응답 dto의 folderId을 넣어주세요. \n
            """)
    ApiResponse<ReviewResponseDto> updateReview(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewUpdateRequestDto reviewUpdateRequestDto);

    @Operation(summary = "리뷰 삭제하기 API", description = "모델이 리뷰를 삭제할 때 사용하는 API입니다.")
    ApiResponse<String> deleteReview(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long reviewId);

    @Operation(summary = "나의 리뷰 내역 조회", description = """
            내가 작성한 리뷰들을 조회하는 API입니다. \n
            `cursorId`: response에서의 nextCursor값을 넣어주시면 됩니다. \n
            `size` : 한 페이지에서 보여질 리뷰의 개수 \n
            """)
    ApiResponse<ScrollResponse<MyReviewListResponseDto>> getReviewList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size);
}
