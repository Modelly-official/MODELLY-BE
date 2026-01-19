package modelly.modelly_be.domain.like.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.controller.swagger.LikeSwagger;
import modelly.modelly_be.domain.like.dto.response.LikeDesignerListResponseDto;
import modelly.modelly_be.domain.like.dto.response.LikeRecruitmentListResponseDto;
import modelly.modelly_be.domain.like.service.LikeService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LikeController implements LikeSwagger {

    private final LikeService likeService;

    @PostMapping("/likes/recruitments/{recruitmentId}")
    public ApiResponse<String> recruitmentLikeOrLikeCancel(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId) {
        likeService.recruitmentLikeOrLikeCancel(authDetails.user(), recruitmentId);

        return ApiResponse.onSuccess("공고 찜 / 찜 취소가 완료되었습니다.");
    }

    //모델이 찜한 공고 리스트 조회
    @GetMapping("/likes/recruitments")
    public ApiResponse<ScrollResponse<LikeRecruitmentListResponseDto>> getLikeRecruitmentList(@AuthenticationPrincipal AuthDetails authDetails,
                                                                                              @RequestParam(required = false) Category category,
                                                                                              @RequestParam(required = false) Long cursorId,
                                                                                              @RequestParam(defaultValue = "20") int size) {

        ScrollResponse<LikeRecruitmentListResponseDto> responseDtos = likeService.getLikeRecruitmentList(authDetails.user(), category, cursorId, size);

        return ApiResponse.onSuccess(responseDtos);
    }

    @PostMapping("/likes/designers/{designerId}")
    public ApiResponse<String> designerLikeOrLikeCancel(@AuthenticationPrincipal AuthDetails authDetails,@PathVariable Long designerId) {
        likeService.designerLikeOrLikeCancel(authDetails.user(),designerId);

        return ApiResponse.onSuccess("디자이너 찜 / 찜 취소가 완료되었습니다.");
    }

    //모델이 찜한 디자이너 리스트 조회
    @GetMapping("/likes/designers")
    public ApiResponse<ScrollResponse<LikeDesignerListResponseDto>> getLikeDesignerList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size) {

        ScrollResponse<LikeDesignerListResponseDto> responseDtos = likeService.getLikeDesignerList(authDetails.user(), category, cursorId, size);

        return ApiResponse.onSuccess(responseDtos);
    }

}
