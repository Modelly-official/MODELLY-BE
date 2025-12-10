package modelly.modelly_be.domain.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.controller.swagger.ModelReviewSwagger;
import modelly.modelly_be.domain.review.dto.request.ReviewCreateRequestDto;
import modelly.modelly_be.domain.review.dto.response.ReviewResponseDto;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.service.ReviewService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ModelReviewController implements ModelReviewSwagger {

    private final ReviewService reviewService;

    @PostMapping("/reviews/{reservationId}")
    public ApiResponse<ReviewResponseDto> createReview(@AuthenticationPrincipal  AuthDetails authDetails, @PathVariable Long reservationId, @RequestBody @Valid ReviewCreateRequestDto reviewCreateRequestDto) {
        Review review = reviewService.createReview(authDetails.user(), reservationId, reviewCreateRequestDto);

        ReviewResponseDto responseDto = ReviewResponseDto.of(review);

        return ApiResponse.onSuccess(responseDto);
    }


}
