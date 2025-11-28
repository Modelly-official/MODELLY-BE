package modelly.modelly_be.domain.recruitment.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.dto.RecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.RecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.service.DesignerRecruitmentService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DesignerRecruitmentController {
    
    private final DesignerRecruitmentService designerRecruitmentService;

    @PostMapping("/recruitments")
    public ApiResponse<RecruitmentResponseDto> createRecruitment(@AuthenticationPrincipal AuthDetails authDetails,
                                                                 @RequestBody RecruitmentRequestDto recruitmentRequestDto) {
        Recruitment recruitment = designerRecruitmentService.createRecruitment(authDetails.user(), recruitmentRequestDto);

        RecruitmentResponseDto responseDto = RecruitmentResponseDto.from(recruitment);

        return ApiResponse.onSuccess(responseDto);
    }
}
