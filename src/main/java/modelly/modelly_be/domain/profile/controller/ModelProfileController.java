package modelly.modelly_be.domain.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.profile.controller.swagger.ModelProfileSwagger;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerMyPageRequest;
import modelly.modelly_be.domain.profile.dto.request.UpdateModelMyPageRequest;
import modelly.modelly_be.domain.profile.dto.response.DesignerMyPageResponse;
import modelly.modelly_be.domain.profile.dto.response.ModelMyPageResponse;
import modelly.modelly_be.domain.profile.service.ModelMyPageService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ModelProfileController implements ModelProfileSwagger {

    private final ModelMyPageService modelMyPageService;

    /* ---------- 모델 마이페이지 프로필 관련 API ---------- */

    // 마이페이지 내 정보(프로필) 조회
    @GetMapping("/models/mypage/profiles")
    public ApiResponse<ModelMyPageResponse> getMyPage(
            @AuthenticationPrincipal AuthDetails auth
    ) {
        return ApiResponse.onSuccess(modelMyPageService.getMyPage(auth.user()));
    }

    @PutMapping("/models/mypage/profiles")
    public ApiResponse<ModelMyPageResponse> updateMyPage(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestBody @Valid UpdateModelMyPageRequest req
    ) {
        return ApiResponse.onSuccess(modelMyPageService.updateMyPage(auth.user(), req));
    }
}
