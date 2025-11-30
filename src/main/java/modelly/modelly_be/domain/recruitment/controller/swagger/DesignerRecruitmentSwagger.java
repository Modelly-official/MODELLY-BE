package modelly.modelly_be.domain.recruitment.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.recruitment.dto.request.RecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.request.UpdateRecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "디자이너 공고관련 API", description = "공고 CUD, 내 공고 리스트 조회")
public interface DesignerRecruitmentSwagger {

    @Operation(summary = "공고 생성하기", description = "디자이너가 공고 생성 시 사용하는 API입니다.")
    ApiResponse<RecruitmentResponseDto> createRecruitment(@AuthenticationPrincipal AuthDetails authDetails, @RequestBody @Valid RecruitmentRequestDto recruitmentRequestDto);

    @Operation(summary = "공고 수정하기", description = "디자이너가 공고 수정 시 사용하는 API입니다")
    ApiResponse<RecruitmentResponseDto> updateRecruitment(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId, @RequestBody @Valid UpdateRecruitmentRequestDto requestDto);

    @Operation(summary = "공고 삭제하기", description = "디자이너가 공고 삭제 시 사용하는 API입니다.")
    ApiResponse<String> deleteRecruitment(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId);

//    @Operation(summary = "내 공고 리스트 조회하기.", description = "디자이너가 자신의 공고리스트를 조회할 때 사용하는 API입니다.")
//    ApiResponse<DesignerRecruitmentListResponseDto> getMyRecruitments(@AuthenticationPrincipal AuthDetails authDetails);

    }
