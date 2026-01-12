package modelly.modelly_be.domain.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.controller.swagger.ModelReviewSwagger;
import modelly.modelly_be.domain.review.dto.request.ReviewCreateRequestDto;
import modelly.modelly_be.domain.review.dto.request.ReviewUpdateRequestDto;
import modelly.modelly_be.domain.review.dto.response.MyReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewResponseDto;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.service.ReviewService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/models")
public class ModelReviewController implements ModelReviewSwagger {

    private final ReviewService reviewService;

    @PostMapping("/reviews/{reservationId}")
    public ApiResponse<ReviewResponseDto> createReview(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long reservationId, @RequestBody @Valid ReviewCreateRequestDto reviewCreateRequestDto) {
        Review review = reviewService.createReview(authDetails.user(), reservationId, reviewCreateRequestDto);

        ReviewResponseDto responseDto = ReviewResponseDto.of(review, true);

        return ApiResponse.onSuccess(responseDto);
    }

    @PutMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewResponseDto> updateReview(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long reviewId, @RequestBody @Valid ReviewUpdateRequestDto reviewUpdateRequestDto) {
        Review review = reviewService.updateReview(authDetails.user(), reviewId, reviewUpdateRequestDto);

        ReviewResponseDto responseDto = ReviewResponseDto.of(review, true);

        return ApiResponse.onSuccess(responseDto);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<String> deleteReview(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long reviewId) {

        reviewService.deleteReview(authDetails.user(), reviewId);

        return ApiResponse.onSuccess("리뷰가 삭제되었습니다.");
    }

    //자신이 작성한 리뷰 내역 조회
    @GetMapping("/reviews")
    public ApiResponse<ScrollResponse<MyReviewListResponseDto>> getReviewList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size){

        List<MyReviewListResponseDto> listDtos = reviewService.getReviewList(authDetails.user(), category, cursorId, size);

        ScrollResponse<MyReviewListResponseDto> responseDtos = ScrollUtil.paginate(listDtos, size);

        return ApiResponse.onSuccess(responseDtos);
    }
}
