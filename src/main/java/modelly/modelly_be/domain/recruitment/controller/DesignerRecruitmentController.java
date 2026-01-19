package modelly.modelly_be.domain.recruitment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.controller.swagger.DesignerRecruitmentSwagger;
import modelly.modelly_be.domain.recruitment.dto.request.RecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.request.UpdateRecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponse;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.domain.recruitment.service.DesignerRecruitmentService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequestMapping("/designers")
@RestController
@RequiredArgsConstructor
public class DesignerRecruitmentController implements DesignerRecruitmentSwagger {
    
    private final DesignerRecruitmentService designerRecruitmentService;

    //공고 생성
    @PostMapping("/recruitments")
    public ApiResponse<RecruitmentResponseDto> createRecruitment(@AuthenticationPrincipal AuthDetails authDetails,
                                                                 @RequestBody @Valid RecruitmentRequestDto recruitmentRequestDto) {
        Recruitment recruitment = designerRecruitmentService.createRecruitment(authDetails.user(), recruitmentRequestDto);

        RecruitmentResponseDto responseDto = RecruitmentResponseDto.from(recruitment);

        return ApiResponse.onSuccess(responseDto);
    }

    //공고 수정
    @PutMapping("/recruitments/{recruitmentId}")
    public ApiResponse<RecruitmentResponseDto> updateRecruitment(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId, @RequestBody @Valid UpdateRecruitmentRequestDto requestDto){
        RecruitmentResponseDto responseDto = designerRecruitmentService.updateRecruitment(authDetails.user(), recruitmentId, requestDto);
        return ApiResponse.onSuccess(responseDto);
    }

    //공고 삭제
    @DeleteMapping("/recruitments/{recruitmentId}")
    public ApiResponse<String> deleteRecruitment(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId){
        designerRecruitmentService.deleteRecruitment(authDetails.user(),recruitmentId);
        return ApiResponse.onSuccess("공고가 삭제되었습니다.");
    }

    //내 공고 리스트 조회하기
    @GetMapping("/recruitments")
    public ApiResponse<ScrollResponse<DesignerRecruitmentListResponse>> getMyRecruitments(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam String month,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RecruitmentStatus status,
            @RequestParam(required = false) LocalDate cursorEarliestDate,
            @RequestParam(required = false) Long cursorId
    ) {
        ScrollResponse<DesignerRecruitmentListResponse> responseDtos = designerRecruitmentService.getDesginerRecruitments(authDetails.user(),month, size, cursorEarliestDate, cursorId, status);

        return ApiResponse.onSuccess(responseDtos);
    }

}
