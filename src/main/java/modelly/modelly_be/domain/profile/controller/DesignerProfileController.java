package modelly.modelly_be.domain.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.profile.controller.swagger.DesignerProfileSwagger;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerProfileRequest;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse;
import modelly.modelly_be.domain.profile.service.DesignerProfileService;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DesignerProfileController implements DesignerProfileSwagger {

    private final DesignerProfileService designerProfileService;

    // 모델/게스트용 특정 디자이너 프로필 조회
    @GetMapping("/profiles/designers/{designerId}")
    public ApiResponse<DesignerProfileResponse> getPublicProfile(
            @AuthenticationPrincipal AuthDetails auth, // null 가능
            @PathVariable Long designerId
    ) {
        return ApiResponse.onSuccess(designerProfileService.getPublicDesignerProfile(auth == null ? null : auth.user(), designerId));
    }

    // 디자이너 본인 프로필 조회
    @GetMapping("/designers/profiles")
    public ApiResponse<DesignerProfileResponse> getMyProfile(
            @AuthenticationPrincipal AuthDetails auth
    ) {
        return ApiResponse.onSuccess(designerProfileService.getMyDesignerProfile(auth.user()));
    }

    // 디자이너 본인 프로필 수정
    @PutMapping("/designers/profiles")
    public ApiResponse<DesignerProfileResponse> updateMyProfile(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestBody @Valid UpdateDesignerProfileRequest request
    ) {
        return ApiResponse.onSuccess(
                designerProfileService.updateMyDesignerProfile(auth.user(), request)
        );
    }
}
