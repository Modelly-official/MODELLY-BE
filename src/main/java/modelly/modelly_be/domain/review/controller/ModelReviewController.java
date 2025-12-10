package modelly.modelly_be.domain.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.controller.swagger.ModelReviewSwagger;
import modelly.modelly_be.domain.review.dto.request.ReviewCreateRequestDto;
import modelly.modelly_be.domain.review.dto.request.ReviewUpdateRequestDto;
import modelly.modelly_be.domain.review.dto.response.ReviewResponseDto;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.service.ReviewService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ModelReviewController implements ModelReviewSwagger {

    private final ReviewService reviewService;

    @PostMapping("/reservations/{reservationId}/reviews")
    public ApiResponse<ReviewResponseDto> createReview(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long reservationId, @RequestBody @Valid ReviewCreateRequestDto reviewCreateRequestDto) {
        Review review = reviewService.createReview(authDetails.user(), reservationId, reviewCreateRequestDto);

        ReviewResponseDto responseDto = ReviewResponseDto.of(review);

        return ApiResponse.onSuccess(responseDto);
    }

    @PutMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewResponseDto> updateReview(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long reviewId, @RequestBody @Valid ReviewUpdateRequestDto reviewUpdateRequestDto) {
        Review review = reviewService.updateReview(authDetails.user(), reviewId, reviewUpdateRequestDto);

        ReviewResponseDto responseDto = ReviewResponseDto.of(review);

        return ApiResponse.onSuccess(responseDto);
    }


}
