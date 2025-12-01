package modelly.modelly_be.domain.like.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.controller.swagger.LikeSwagger;
import modelly.modelly_be.domain.recruitment.service.LikeService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController implements LikeSwagger {

    private final LikeService likeService;

    @PostMapping("/likes/{recruitmentId}")
    public ApiResponse<String> recruitmentLikeOrLikeCancel(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId) {
        likeService.recruitmentLikeOrLikeCancel(authDetails.user(), recruitmentId);

        return ApiResponse.onSuccess("찜 / 찜 취소가 완료되었습니다.");
    }

}
