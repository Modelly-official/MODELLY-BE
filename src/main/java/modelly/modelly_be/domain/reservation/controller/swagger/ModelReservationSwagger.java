package modelly.modelly_be.domain.reservation.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCreateRequest;
import modelly.modelly_be.domain.reservation.dto.response.AvailableReservationScheduleResponse;
import modelly.modelly_be.domain.reservation.dto.response.ModelReservationItem;
import modelly.modelly_be.domain.reservation.dto.response.ReservationScrollResponse;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(
        name = "모델용 예약 API",
        description = "예약하기, 예약 가능한 시간대 조회, 예약 목록 조회, 리뷰 미작성 완료 예약 조회"
)
public interface ModelReservationSwagger {
    /* ---------- 모델 관련 예약 API ---------- */

    @Operation(
            summary = "예약하기(모델)",
            description = """
                    ### 모델이 공고(recruitment)의 특정 시간대로 예약을 신청하는 API입니다. \n
                    - 예약은 생성 시 상태가 PENDING으로 저장됩니다. \n
                    \n
                    ---\n
                    ### Request Body\n
                    - `recruitmentId` : 예약을 생성할 공고 ID\n
                    - `date` : 예약 날짜 (yyyy-MM-dd)\n
                    - `startTime` : 예약 시작 시간 (HH:mm)\n
                    - `category` : 예약 카테고리(대분류) (ex. HAIR)\n
                    - `subCategories` : 세부 카테고리 목록 (복수 선택 가능)\n
                    - `comment` : 요청 사항/추가 설명\n
                    - `designerName` : 디자이너명(닉네임)\n
                    - `shop` : 시술 장소\n
                    - `imageUrls` : 참고 이미지 URL 리스트\n
                    \n
                    ---\n
                    ### 동작\n
                    - 디자이너 스케줄 conflict(대기/확정) 여부 확인\n
                    - Reservation 생성 및 상태 PENDING으로 저장\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 모델 권한만 호출 가능합니다.\n
                    """
    )
    ApiResponse<SimpleMessageDTO> createReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestBody @Valid ReservationCreateRequest request
    );

    @Operation(
            summary = "예약 가능한 시간대 조회(모델)",
            description = """
                    ### 특정 공고(recruitmentId)에 대해 월 단위로 예약 가능한 시간대를 조회하는 API입니다. \n
                    \n
                    ---\n
                    ### Request Param\n
                    - `recruitmentId` : 공고 ID (필수)\n
                    - `month`(optional) : 조회할 월 (yyyy-MM). 없으면 현재 월 기준\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 모델 권한만 호출 가능합니다.\n
                    """
    )
    ApiResponse<AvailableReservationScheduleResponse> getAvailableSchedules(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam Long recruitmentId,
            @RequestParam(required = false) String month
    );

    @Operation(
            summary = "예약 목록 조회(모델) - 무한스크롤",
            description = """
                    ### 모델의 예약 목록을 무한 스크롤로 조회하는 API입니다. \n
                    \n
                    ---\n
                    ### Request Param\n
                    - `month`(optional) : yyyy-MM (미설정 시 서버에서 기본 월 설정)\n
                    - `type` : UPCOMING / COMPLETED\n
                    - `category`(optional) : 카테고리 필터 (전체 조회면 null)\n
                    - `size`(default=12) : 페이지 사이즈\n
                    - `cursorDate`(optional) : 커서 날짜(yyyy-MM-dd)\n
                    - `cursorTime`(optional) : 커서 시간(HH:mm)\n
                    - `cursorId`(optional) : 커서 예약 ID\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 모델 권한만 호출 가능합니다.\n
                    """
    )
    ApiResponse<ReservationScrollResponse<ModelReservationItem>> getModelReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam(required = false) String month,
            @RequestParam ReservationListType type,
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime,
            @RequestParam(required = false) Long cursorId
    );

    @Operation(
            summary = "리뷰 미작성 완료 예약 조회(모델)",
            description = """
                    ### 모델의 '완료된 예약 중 리뷰 미작성' 예약만 무한 스크롤로 조회하는 API입니다. \n
                    \n
                    ---\n
                    ### Request Param\n
                    - `month`(optional) : yyyy-MM\n
                    - `category`(optional) : 카테고리 필터 (전체 조회면 null)\n
                    - `size`(default=12) : 페이지 사이즈\n
                    - `cursorDate/cursorTime/cursorId`(optional) : 무한스크롤 커서\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 모델 권한만 호출 가능합니다.\n
                    """
    )
    ApiResponse<ReservationScrollResponse<ModelReservationItem>> getUnreviewedCompletedReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime,
            @RequestParam(required = false) Long cursorId
    );
}
