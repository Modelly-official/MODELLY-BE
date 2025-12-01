package modelly.modelly_be.domain.recruitment.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.controller.swagger.GuestRecruitmentSwagger;
import modelly.modelly_be.domain.recruitment.dto.common.RecruitmentCursor;
import modelly.modelly_be.domain.recruitment.dto.response.GuestRecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.entity.SubCategory;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SortOption;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.UserCoordinate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GuestRecruitmentController implements GuestRecruitmentSwagger {
    private final RecruitmentService recruitmentService;

    @GetMapping("/recruitments/{recruitmentId}")
    public ApiResponse<GuestRecruitmentResponseDto> getRecruitment(@PathVariable Long recruitmentId){

        GuestRecruitmentResponseDto responseDto = recruitmentService.getByIdWithDesigner(recruitmentId);

        return ApiResponse.onSuccess(responseDto);
    }

    @GetMapping("/recruitments")
    public ApiResponse<ScrollResponse<RecruitmentListResponseDto>> getRecruitmentList(
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
            @RequestParam(required = false) Double userLongitude){

        SearchCondition searchCondition = new SearchCondition(category,subCategory,keyword);
        RecruitmentCursor recruitmentCursor = new RecruitmentCursor(cursorId,cursorReviewCount,cursorDistance);
        UserCoordinate userCoordinate = new UserCoordinate(userLatitude, userLongitude);
        Long userId = (authDetails != null ? authDetails.user().getId() : null);

        List<RecruitmentListResponseDto> recruitments = recruitmentService.getRecruitmensList(userId, searchCondition, sortOption, recruitmentCursor, size, userCoordinate);

        ScrollResponse<RecruitmentListResponseDto> responseDtos = ScrollUtil.paginate(recruitments,size);

        return ApiResponse.onSuccess(responseDtos);
    }
}
