package modelly.modelly_be.domain.home.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.home.dto.response.ModelHomeReservationResponse;
import modelly.modelly_be.domain.home.dto.response.PopularRecruitmentListResponse;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "모델 홈 관련 API", description = "예약 내역 조회, 주위 모집글 조회, 실시간 인기 TOP 모집글 조회, 시술별 인기 디자이너 조회")
public interface ModelHomeSwagger {

    @GetMapping("/home/models/reservations")
    @Operation(summary = "모델 예약 내역 조회 API", description = "모델이 홈에서 예약 내역을 조회할 때 사용하는 API입니다.")
    ApiResponse<List<ModelHomeReservationResponse>> getModelReservationsAtHome(
            @AuthenticationPrincipal AuthDetails authDetails
    );

    @GetMapping("/recruitments/nearby")
    @Operation(summary = "내 주위 모집글 조회 API", description = """
            모델이 홈에서 내 주위 모집글을 조회할 때 사용하는 API입니다. \n
            ### Request Parameter
            `category`: HAIR, NAIL, TATTOO, EYELASH 중 택1 \n
            `userLatitude` : 사용자의 위도 \n
            `userLongitude`: 사용자의 경도 \n
            """)
    ApiResponse<List<RecruitmentListResponseDto>> getRecruitmentsNearby(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) Category category,
            @RequestParam Double userLatitude,
            @RequestParam Double userLongitude
            );

    @GetMapping("/recruitments/popular")
    @Operation(summary = "실시간 인기 TOP 모집글 조회 API", description = """
            모델이 홈에서 실시간 인기 TOP 모집글을 조회할 때 사용하는 API입니다. \n
            ### Request Parameter
            `category`: HAIR, NAIL, TATTOO, EYELASH 중 택1 \n
            """)
    ApiResponse<List<PopularRecruitmentListResponse>> getPopularRecruitments(
            @RequestParam(required = false) Category category
    );

    @GetMapping("/designers/popular")
    @Operation(summary = "시술별 인기 디자이너 조회", description = """
            모델이 홈에서 시술별 인기 디자이너를 조회할 때 사용하는 API입니다. \n
            ### Request Parameter
            `category`: HAIR, NAIL, TATTOO, EYELASH 중 택1 \n
            `userLatitude` : 사용자의 위도 \n
            `userLongitude`: 사용자의 경도 \n
            """)
    ApiResponse<List<DesignerListResponseDto>> getPopularDesigners(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam Category category,
            @RequestParam Double userLatitude,
            @RequestParam Double userLongitude
    );

}
