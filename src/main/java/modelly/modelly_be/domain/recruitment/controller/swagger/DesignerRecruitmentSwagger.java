package modelly.modelly_be.domain.recruitment.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.recruitment.dto.request.RecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.request.UpdateRecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.internal.DesignerRecruitmentList;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponse;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "디자이너 공고관련 API", description = "공고 CUD, 내 공고 리스트 조회")
public interface DesignerRecruitmentSwagger {

    @Operation(summary = "공고 생성하기", description = "디자이너가 공고 생성 시 사용하는 API입니다.")
    ApiResponse<RecruitmentResponseDto> createRecruitment(@AuthenticationPrincipal AuthDetails authDetails, @RequestBody @Valid RecruitmentRequestDto recruitmentRequestDto);

    @Operation(summary = "공고 수정하기", description = "디자이너가 공고 수정 시 사용하는 API입니다")
    ApiResponse<RecruitmentResponseDto> updateRecruitment(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId, @RequestBody @Valid UpdateRecruitmentRequestDto requestDto);

    @Operation(summary = "공고 삭제하기", description = "디자이너가 공고 삭제 시 사용하는 API입니다.")
    ApiResponse<String> deleteRecruitment(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long recruitmentId);

    @Operation(summary = "내 공고 리스트 조회하기.", description = """
            디자이너가 자신의 공고리스트를 조회할 때 사용하는 API입니다. </p>
            공고를 확인하는 해당 달을 request Param으로 같이 보내주세요. </p>
            
            ### Request Param </p>
            `month` : 공고를 확인하는 해당 달 ex) 2025-10
            
            \n
            ---\n
            ### Response </p>
            `hasPendingReservation` : 대기 중인 예약 존재 유무 \n
            `hasConfirmedReservation` : 확정된 예약 중 아직 진행하지 않은 예약 존재 유무 \n
            `canModify` : 수정/삭제 가능 여부
            """)
    ApiResponse<ScrollResponse<DesignerRecruitmentListResponse>> getMyRecruitments(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam String month,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RecruitmentStatus status,
            @RequestParam(required = false) LocalDate cursorEarliestDate,
            @RequestParam(required = false) Long cursorId);

    }
