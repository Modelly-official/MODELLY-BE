package modelly.modelly_be.domain.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerProfileRequest;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse;
import modelly.modelly_be.domain.profile.service.DesignerProfileService;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class DesignerProfileController {

    private final DesignerProfileService designerProfileService;

    // 특정 디자이너 프로필 조회(무인증)
    @GetMapping("/profiles/designers/{designerId}")
    public ApiResponse<DesignerProfileResponse> getPublicProfile(
            @PathVariable Long designerId
    ) {
        return ApiResponse.onSuccess(designerProfileService.getPublicDesignerProfile(designerId));
    }

    // 디자이너 본인 프로필 조회
    @GetMapping("/designers/profiles/me")
    public ApiResponse<DesignerProfileResponse> getMyProfile(
            @AuthenticationPrincipal AuthDetails auth
    ) {
        return ApiResponse.onSuccess(designerProfileService.getMyDesignerProfile(auth.user()));
    }

    // 디자이너 본인 프로필 수정
    @PatchMapping("/designers/profiles/me")
    public ApiResponse<DesignerProfileResponse> updateMyProfile(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestBody @Valid UpdateDesignerProfileRequest request
    ) {
        return ApiResponse.onSuccess(
                designerProfileService.updateMyDesignerProfile(auth.user(), request)
        );
    }
}
