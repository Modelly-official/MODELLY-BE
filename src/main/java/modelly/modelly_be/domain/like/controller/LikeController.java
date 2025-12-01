package modelly.modelly_be.domain.like.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.controller.swagger.LikeSwagger;
import modelly.modelly_be.domain.like.dto.LikeRecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.service.LikeService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LikeController implements LikeSwagger {

    private final LikeService likeService;

    @PostMapping("/likes/{recruitmentId}")
    public ApiResponse<String> recruitmentLikeOrLikeCancel(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId) {
        likeService.recruitmentLikeOrLikeCancel(authDetails.user(), recruitmentId);

        return ApiResponse.onSuccess("찜 / 찜 취소가 완료되었습니다.");
    }

    @GetMapping("/likes/recruitments")
    public ApiResponse<ScrollResponse<LikeRecruitmentListResponseDto>> getLikeRecruitmentList(AuthDetails authDetails, Category category, Long cursorId, int size) {

        List<LikeRecruitmentListResponseDto> likeList = likeService.getLikeRecruitmentList(authDetails.user(), category, cursorId, size);

        ScrollResponse<LikeRecruitmentListResponseDto> responseDtos = ScrollUtil.paginate(likeList,size);
        return ApiResponse.onSuccess(responseDtos);
    }

}
