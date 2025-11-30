package modelly.modelly_be.domain.recruitment.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import modelly.modelly_be.domain.recruitment.dto.response.GuestRecruitmentResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;


@Tag(name = "로그인X 상태에서의 공고관련 API", description = "공고별로 보기, 디자이너별로 보기, 공고 상세 조회, 공고 검색")
public interface GuestRecruitmentSwagger {

    @Operation(summary = "공고 상세 조회하기", description = "공고 상세 조회 시 사용하는 API입니다. (로그인 필요X)")
    ApiResponse<GuestRecruitmentResponseDto> getRecruitment(@PathVariable Long recruitmentId);
}
