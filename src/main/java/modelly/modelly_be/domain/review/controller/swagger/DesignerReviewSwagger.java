package modelly.modelly_be.domain.review.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.review.dto.request.ReplyRequestDto;
import modelly.modelly_be.domain.review.dto.response.DesignerReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReplyResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "디자이너 뷰에서의 리뷰 관련 API", description = "리뷰 고정 / 고정 취소, 답글 달기, 답글 수정하기")
public interface DesignerReviewSwagger {

    @Operation(summary = "리뷰 고정 / 고정 취소하기 API", description = "디자이너가 리뷰를 고정하거나 고정 취소할 때 사용하는 API입니다.")
    ApiResponse<String> fixReview(@AuthenticationPrincipal AuthDetails authDetails, @RequestParam Long reviewId);

    @Operation(summary = "답글 달기 API", description = """
            디자이너가 특정 리뷰에 대해 답글을 달 때 사용하는 API입니다. \n
            ---
            Request Body \n
            `content` : 답글 내용으로, 200자 이하여야합니다.
            """)
    ApiResponse<ReplyResponseDto> createReply(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long reviewId,
            @RequestBody @Valid ReplyRequestDto requestDto);

    @Operation(summary = "답글 수정 API", description = """
           디자이너가 답글을 수정할 때 사용하는 API입니다. \n
            ---
            Request Body \n
            `content` : 답글 내용으로, 200자 이하여야합니다.
            """)
    ApiResponse<ReplyResponseDto> updateReply(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long replyId,
            @RequestBody @Valid ReplyRequestDto requestDto);

    @Operation(summary = "나(디자이너)에 대한 리뷰 리스트 조회 API", description = """
            디자이너가 본인에게 달린 리뷰 리스트를 조회하는 API입니다. \n
            `cursorId`: response에서의 nextCursor값을 넣어주시면 됩니다. \n
            `size` : 한 페이지에서 보여질 리뷰의 개수 \n
            """)
    ApiResponse<ScrollResponse<DesignerReviewListResponseDto>> getDesignerReviewList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    );
}
