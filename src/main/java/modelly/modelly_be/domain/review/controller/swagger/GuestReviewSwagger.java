package modelly.modelly_be.domain.review.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import modelly.modelly_be.domain.review.dto.response.ReviewListResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface GuestReviewSwagger {

    @Operation(summary = "디자이너 리뷰 리스트 조회 API", description = """
            해당 디자이너의 리뷰 리스트를 조회하는 API입니다. \n
            `cursorId`: response에서의 nextCursor값을 넣어주시면 됩니다. \n
            `size` : 한 페이지에서 보여질 리뷰의 개수 \n
            """)
    ApiResponse<ScrollResponse<ReviewListResponseDto>> getDesignerReviewList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long designerId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
            );
}
