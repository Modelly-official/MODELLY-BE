package modelly.modelly_be.domain.like.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.controller.swagger.LikeSwagger;
import modelly.modelly_be.domain.like.dto.LikeRecruitmentListResponseDto;
import modelly.modelly_be.domain.like.service.LikeService;
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
public class LikeController implements LikeSwagger {

    private final LikeService likeService;

    @PostMapping("/likes/recruitments/{recruitmentId}")
    public ApiResponse<String> recruitmentLikeOrLikeCancel(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId) {
        likeService.recruitmentLikeOrLikeCancel(authDetails.user(), recruitmentId);

        return ApiResponse.onSuccess("공고 찜 / 찜 취소가 완료되었습니다.");
    }

    @GetMapping("/likes/recruitments")
    public ApiResponse<ScrollResponse<LikeRecruitmentListResponseDto>> getLikeRecruitmentList(@AuthenticationPrincipal AuthDetails authDetails,
                                                                                              @RequestParam(required = false) Category category,
                                                                                              @RequestParam(required = false) Long cursorId,
                                                                                              @RequestParam(defaultValue = "20") int size) {

        List<LikeRecruitmentListResponseDto> likeList = likeService.getLikeRecruitmentList(authDetails.user(), category, cursorId, size);

        ScrollResponse<LikeRecruitmentListResponseDto> responseDtos = ScrollUtil.paginate(likeList,size);
        return ApiResponse.onSuccess(responseDtos);
    }

    @PostMapping("/likes/designers/{designerId}")
    public ApiResponse<String> designerLikeOrLikeCancel(AuthDetails authDetails,@PathVariable Long designerId) {
        likeService.designerLikeOrLikeCancel(authDetails.user(),designerId);

        return ApiResponse.onSuccess("디자이너 찜 / 찜 취소가 완료되었습니다.");
    }

}
