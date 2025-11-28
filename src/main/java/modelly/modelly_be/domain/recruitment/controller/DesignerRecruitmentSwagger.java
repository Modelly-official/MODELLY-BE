package modelly.modelly_be.domain.recruitment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.recruitment.dto.RecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.RecruitmentResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "디자이너 공고관련 API", description = "공고 CRUD, 내 공고 리스트 조회")
public interface DesignerRecruitmentSwagger {

    @Operation(summary = "공고 생성하기", description = "디자이너가 공고 생성 시 사용하는 API입니다.")
    ApiResponse<RecruitmentResponseDto> createRecruitment(@AuthenticationPrincipal AuthDetails authDetails, @RequestBody @Valid RecruitmentRequestDto recruitmentRequestDto);
}
