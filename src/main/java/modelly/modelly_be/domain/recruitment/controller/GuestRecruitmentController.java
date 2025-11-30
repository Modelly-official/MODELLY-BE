package modelly.modelly_be.domain.recruitment.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.controller.swagger.GuestRecruitmentSwagger;
import modelly.modelly_be.domain.recruitment.dto.response.GuestRecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GuestRecruitmentController implements GuestRecruitmentSwagger {
    private final RecruitmentService recruitmentService;

    @GetMapping("/recruitments/{recruitmentId}")
    public ApiResponse<GuestRecruitmentResponseDto> getRecruitment(@PathVariable Long recruitmentId){

        GuestRecruitmentResponseDto responseDto = recruitmentService.getByIdWithDesigner(recruitmentId);

        return ApiResponse.onSuccess(responseDto);
    }
}
