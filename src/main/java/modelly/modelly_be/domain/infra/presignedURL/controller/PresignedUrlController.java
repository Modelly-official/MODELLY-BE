package modelly.modelly_be.domain.infra.presignedURL.controller;

import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.infra.presignedURL.controller.swagger.PresignedUrlSwagger;
import modelly.modelly_be.domain.infra.presignedURL.dto.PresignedUrlListResponse;
import modelly.modelly_be.domain.infra.presignedURL.service.PresignedUrlService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.s3.PresignedUploadResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PresignedUrlController implements PresignedUrlSwagger {

    private final PresignedUrlService presignedUrlService;

    @GetMapping("/presigned-url/recruitments")
    public ApiResponse<PresignedUrlListResponse> createRecruitmentImage(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam @Max(3) int imageCount
            ) {

        PresignedUrlListResponse response = presignedUrlService.createRecruitmentImage(authDetails.user(), imageCount);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/presigned-url/reviews")
    public ApiResponse<PresignedUrlListResponse> createReviewsImage(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam Long reservationId,
            @RequestParam @Max(3) int imageCount
    ) {

        PresignedUrlListResponse response = presignedUrlService.createReviewsImage(authDetails.user(), reservationId, imageCount);

        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/presigned-url/chats/{roomId}/")
    public ApiResponse<PresignedUploadResponse> createPresignedUrl(
            @PathVariable Long roomId,
            @AuthenticationPrincipal AuthDetails auth
    ) {
        PresignedUploadResponse response = presignedUrlService.createChattingImage(auth.user(), roomId);
        return ApiResponse.onSuccess(response);
    }
}
