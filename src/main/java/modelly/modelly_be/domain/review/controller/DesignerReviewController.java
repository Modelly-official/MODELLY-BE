package modelly.modelly_be.domain.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.controller.swagger.DesignerReviewSwagger;
import modelly.modelly_be.domain.review.dto.request.ReplyRequestDto;
import modelly.modelly_be.domain.review.dto.response.DesignerReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReplyResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewListScrollResponse;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.service.DesignerReviewService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DesignerReviewController implements DesignerReviewSwagger {

    private final DesignerReviewService designerReviewService;

    @PatchMapping("/designers/reviews")
    public ApiResponse<String> fixReview(@AuthenticationPrincipal AuthDetails authDetails, @RequestParam Long reviewId){

        designerReviewService.fixReview(authDetails.user(), reviewId);

        return ApiResponse.onSuccess("리뷰 고정 / 고정 취소가 완료되었습니다.");
    }

    @PostMapping("/designers/reviews/{reviewId}")
    public ApiResponse<ReplyResponseDto> createReply(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long reviewId, @RequestBody @Valid ReplyRequestDto requestDto){

        Reply reply = designerReviewService.createReply(authDetails.user(), reviewId, requestDto);

        return ApiResponse.onSuccess(ReplyResponseDto.of(reply));
    }

    @PutMapping("/designers/replies/{replyId}")
    public ApiResponse<ReplyResponseDto> updateReply(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long replyId, @RequestBody @Valid ReplyRequestDto requestDto) {

        Reply reply = designerReviewService.updateReply(authDetails.user(), replyId, requestDto);

        return ApiResponse.onSuccess(ReplyResponseDto.of(reply));
    }

    @GetMapping("/designers/reviews")
    public ApiResponse<ReviewListScrollResponse> getDesignerReviewList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Boolean cursorIsFixed,
            @RequestParam(defaultValue = "10") int size
    ){
        ReviewListScrollResponse responseDtos = designerReviewService.getDesignerReviewList(authDetails.user(), cursorId, cursorIsFixed, size);

        return ApiResponse.onSuccess(responseDtos);
    }
}
