package modelly.modelly_be.domain.recruitment.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.GuestRecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.entity.SubCategory;
import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SortOption;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "로그인X 상태에서의 공고관련 API", description = "공고별로 보기, 디자이너별로 보기, 공고 상세 조회, 공고 검색")
public interface GuestRecruitmentSwagger {

    @Operation(summary = "공고 상세 조회하기", description = "공고 상세 조회 시 사용하는 API입니다. (로그인 필요X)")
    ApiResponse<GuestRecruitmentResponseDto> getRecruitment(@PathVariable Long recruitmentId);

    @Operation(summary = "공고별로 보기", description = """
            ### 공고별로 볼 때 사용하는 API입니다. \n
            ✅ 정렬 : 최신순, 후기 많은순, 거리순 \n
            ✅ 검색 가능 \n
            ✅ 필터링 : 카테고리, 세부카테고리별로 가능 \n
            ---
            ### Request Param \n
            `category` : HAIR, NAIL, TATTOO, MAKEUP, ETC 중 택1 \n
            `subCategory` : 세부 카테고리입니다. \n
            - 헤어 관련: HAIR_CUT, HAIR_PERM, HAIR_COLORING \s
            - 네일 관련: ONE_COLOR, ART, PEDICURE \s
            - 속눈썹 관련: EYELASH_PERM, EYELASH_EXTENSION \s
            - 타투 관련: LIP_TATTOO, EYEBROW_TATTOO, NORMAL_TATTOO \n
                이 중에서 택1 해주세요 \n
            `keyword` : 검색 시 사용 ex. keyword = 레이어드펌 \n
            `sortOption` : 정렬 시 사용. NEWEST(최신순), MOST_REVIEWS(리뷰 많은 순), DISTANCE(거리순) 중 택1 \n
            `cursorId` : 다음 공고 리스트를 가져올 때, 현재 페이지의 마지막 공고의 id를 넣어주시면 됩니다 \n
            `cursorReviewCount` : 리뷰순으로 정렬한 상태에서, 다음 공고 리스트를 가져올 때, 현재 페이지의 마지막 공고의 reviewCount 넣어주시면 됩니다 \n
            `cursorDistance` : 거리순으로 정렬한 상태에서, 다음 공고 리스트를 가져올 때, 현재 페이지의 마지막 공고의 distance를 넣어주시면 됩니다 \n
            `size` : 한 페이지에서 보여질 공고의 개수 \n
            `userLatitude` : 거리순으로 정렬할 경우, 사용자의 위도를 넣어주시면 됩니다. \n
            `userLongitude`: 거리순으로 정렬할 경우, 사용자의 경도를 넣어주시면 됩니다. \n
            ---
            ### 정렬 및 cursor관련 설명
            1. 최신순 + cursor 이용 시 : cursorId에 응답값에 있는 nextCursor값을 넣어주세요. \n
            2. 후기 많은 순 + cursor 이용 시 : cursorId에 응답값에 있는 nextCursor값을, 커서에 해당하는 reviewCount를 cursorReviewCount에 넣어주세요. \n
            3. 거리순 이용 시 : user의 위도, 경도 값을 꼭 입력해주세요. \n
                + cursor 이용 시 : cursorId에 응답값에 있는 nextCursor값을, 커서에 해당하는 reviewDistance를 cursorReviewDistance에 넣어주세요. \n
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

    @Operation(summary = "디자이너별로 보기", description = """
            ### 디자이너별로 볼 때 사용하는 API입니다. \n
            ✅ 정렬 : 최신순, 후기 많은순, 거리순 \n
            ✅ 검색 가능 \n
            ✅ 필터링 : 카테고리, 세부카테고리별로 가능 \n
            ---
            ### Request Param \n
            `category` : HAIR, NAIL, TATTOO, MAKEUP, ETC 중 택1 \n
            `subCategory` : 세부 카테고리입니다. \n
            - 헤어 관련: HAIR_CUT, HAIR_PERM, HAIR_COLORING \s
            - 네일 관련: ONE_COLOR, ART, PEDICURE \s
            - 속눈썹 관련: EYELASH_PERM, EYELASH_EXTENSION \s
            - 타투 관련: LIP_TATTOO, EYEBROW_TATTOO, NORMAL_TATTOO \n
                이 중에서 택1 해주세요 \n
            `keyword` : 검색 시 사용 ex. keyword = 레이어드펌 \n
            `sortOption` : 정렬 시 사용. NEWEST(최신순), MOST_REVIEWS(리뷰 많은 순), DISTANCE(거리순) 중 택1 \n
            `cursorId` : 다음 공고 리스트를 가져올 때, 현재 페이지의 마지막 공고의 id를 넣어주시면 됩니다 \n
            `cursorReviewCount` : 리뷰순으로 정렬한 상태에서, 다음 공고 리스트를 가져올 때, 현재 페이지의 마지막 공고의 reviewCount 넣어주시면 됩니다 \n
            `cursorDistance` : 거리순으로 정렬한 상태에서, 다음 공고 리스트를 가져올 때, 현재 페이지의 마지막 공고의 distance를 넣어주시면 됩니다 \n
            `size` : 한 페이지에서 보여질 공고의 개수 \n
            `userLatitude` : 거리순으로 정렬할 경우, 사용자의 위도를 넣어주시면 됩니다. \n
            `userLongitude`: 거리순으로 정렬할 경우, 사용자의 경도를 넣어주시면 됩니다. \n
            ---
            ### 정렬 및 cursor관련 설명
            1. 최신순 + cursor 이용 시 : cursorId에 응답값에 있는 nextCursor값을 넣어주세요. \n
            2. 후기 많은 순 + cursor 이용 시 : cursorId에 응답값에 있는 nextCursor값을, 커서에 해당하는 reviewCount를 cursorReviewCount에 넣어주세요. \n
            3. 거리순 이용 시 : user의 위도, 경도 값을 꼭 입력해주세요. \n
                + cursor 이용 시 : cursorId에 응답값에 있는 nextCursor값을, 커서에 해당하는 reviewDistance를 cursorReviewDistance에 넣어주세요. \n
            """)
    ApiResponse<ScrollResponse<DesignerListResponseDto>> getDesignerList(
            @AuthenticationPrincipal AuthDetails authDetails, //찜 여부를 위해서
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "NEWEST")SortOption sortOption,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Long cursorReviewCount,
            @RequestParam(required = false) Double cursorDistance,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Double userLatitude,
            @RequestParam(required = false) Double userLongitude);
}
