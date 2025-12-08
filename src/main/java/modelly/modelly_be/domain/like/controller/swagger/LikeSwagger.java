package modelly.modelly_be.domain.like.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.like.dto.response.LikeRecruitmentListResponseDto;
import modelly.modelly_be.domain.like.dto.response.LikeDesignerListResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "찜 관련 API", description = "공고 / 디자이너 찜, 찜 취소, 찜한 리스트 조회")
public interface LikeSwagger {

    @Operation(summary = "공고 찜하기 / 찜 취소하기", description = "모델이 공고를 찜하거나 찜을 취소할 때 사용하는 API입니다.")
    ApiResponse<String> recruitmentLikeOrLikeCancel(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId);

    @Operation(summary = "찜한 공고 리스트 조회하기", description = """
            모델이 찜한 공고리스트를 조회할 때 사용하는 API입니다. \n
            `category` : HAIR, NAIL, TATTOO, EYELASH, ETC 중 택1 \n
            `cursorId` : 다음 페이지를 보여줄 경우 responseDto에서의 nextCursor 값을 넣어주시면 됩니다. \n
            `size` : 한 페이지에서 보여질 찜한 공고의 개수 \n
            """)
    ApiResponse<ScrollResponse<LikeRecruitmentListResponseDto>> getLikeRecruitmentList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size);

    @Operation(summary = "디자이너 찜하기 / 찜 취소하기", description = "모델이 디자이너를 찜하거나 찜을 취소할 때 사용하는 API입니다.")
    ApiResponse<String> designerLikeOrLikeCancel(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long designerId);

    @Operation(summary = "찜한 디자이너 리스트 조회하기", description = """
            모델이 찜한 디자이너 리스트를 조회할 때 사용하는 API입니다. \n
            `category` : HAIR, NAIL, TATTOO, EYELASH, ETC 중 택1 \n
            `cursorId` : 다음 페이지를 보여줄 경우 responseDto에서의 nextCursor 값을 넣어주시면 됩니다. \n
            `size` : 한 페이지에서 보여질 찜한 디자이너의 수 \n
            """)
    ApiResponse<ScrollResponse<LikeDesignerListResponseDto>> getLikeDesignerList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size
    );
}
