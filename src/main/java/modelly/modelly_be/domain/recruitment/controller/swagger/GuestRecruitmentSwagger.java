package modelly.modelly_be.domain.recruitment.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.recruitment.dto.common.RecruitmentCursor;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.GuestRecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.entity.SubCategory;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SortOption;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.UserCoordinate;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "로그인X 상태에서의 공고관련 API", description = "공고별로 보기, 디자이너별로 보기, 공고 상세 조회, 공고 검색")
public interface GuestRecruitmentSwagger {

    @Operation(summary = "공고 상세 조회하기", description = "공고 상세 조회 시 사용하는 API입니다. (로그인 필요X)")
    ApiResponse<GuestRecruitmentResponseDto> getRecruitment(@PathVariable Long recruitmentId);

    @Operation(summary = "공고별로 보기", description = """
            공고별로 볼 때 사용하는 API입니다.
            ✅ 정렬 : 최신순, 후기 많은순, 거리순
            ✅ 검색 가능
            ✅ 필터링 : 카테고리, 세부카테고리별로 가능
            """)
    ApiResponse<ScrollResponse<RecruitmentListResponseDto>> getRecruitmentList(
            @AuthenticationPrincipal AuthDetails authDetails, //찜 여부를 위해서
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) SubCategory subCategory,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "NEWEST")SortOption sortOption,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Long cursorReviewCount,
            @RequestParam(required = false) Double cursorDistance,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Double userLatitude,
            @RequestParam(required = false) Double userLongitude);
}
