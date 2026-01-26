package modelly.modelly_be.domain.review.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.controller.swagger.GuestReviewSwagger;
import modelly.modelly_be.domain.review.dto.response.*;
import modelly.modelly_be.domain.review.service.ReviewService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GuestReviewController implements GuestReviewSwagger {

    private final ReviewService reviewService;


    @Override
    public ApiResponse<ReviewListScrollResponse> getDesignerReviewList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long designerId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Boolean cursorIsFixed,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = (authDetails != null ? authDetails.user().getId() : null);

        ReviewListScrollResponse responseDtos = reviewService.getDesignerReviewList(userId, designerId, cursorId, cursorIsFixed, size);

        return ApiResponse.onSuccess(responseDtos);
    }


    @Override
    public ApiResponse<ScrollResponse<ReviewThumbnailListResponseDto>> getDesignerReviewThumbnailList(
            @PathVariable Long designerId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size) {

        ScrollResponse<ReviewThumbnailListResponseDto> responseDtos = reviewService.getReviewThumbnailList(designerId, cursorId, size);

        return ApiResponse.onSuccess(responseDtos);
    }


    @Override
    public ApiResponse<ReviewListScrollResponse> getDesignerReviewImageList(
            @PathVariable Long designerId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Boolean cursorIsFixed,
            @RequestParam(defaultValue = "10") int size) {

        ReviewListScrollResponse responseDtos = reviewService.getReviewImageList(designerId, cursorId, cursorIsFixed, size);

        return ApiResponse.onSuccess(responseDtos);
    }

    @Override
    public ApiResponse<ReviewResponseDto> getReview(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long reviewId) {

        Long userId = (authDetails != null ? authDetails.user().getId() : null);

        ReviewResponseDto responseDto = reviewService.getReview(userId, reviewId);

        return ApiResponse.onSuccess(responseDto);
    }
}