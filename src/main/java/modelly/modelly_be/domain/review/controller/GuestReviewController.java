package modelly.modelly_be.domain.review.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.controller.swagger.GuestReviewSwagger;
import modelly.modelly_be.domain.review.dto.response.ReviewListResponseDto;
import modelly.modelly_be.domain.review.service.ReviewService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GuestReviewController implements GuestReviewSwagger {

    private final ReviewService reviewService;

    @GetMapping("/{designerId}/reviews")
    public ApiResponse<ScrollResponse<ReviewListResponseDto>> getDesignerReviewList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long designerId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = (authDetails != null ? authDetails.user().getId() : null);

        List<ReviewListResponseDto> dtolist = reviewService.getDesignerReviewList(userId, designerId, cursorId, size);

        ScrollResponse<ReviewListResponseDto> responseDtos = ScrollUtil.paginate(dtolist, size);

        return ApiResponse.onSuccess(responseDtos);
    }
}