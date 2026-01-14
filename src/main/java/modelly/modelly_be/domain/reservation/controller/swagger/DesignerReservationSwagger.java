package modelly.modelly_be.domain.reservation.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.reservation.dto.response.*;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(
        name = "디자이너용 예약 API",
        description = "예약 목록 조회, 오늘의 예약 조회, 새로운 예약 신청 조회, 예약 상세조회, 예약 확정/거절"
)
public interface DesignerReservationSwagger {

    /* ---------- 디자이너 관련 예약 API ---------- */

    @Operation(
            summary = "예약 목록 조회(디자이너) - 무한스크롤",
            description = """
                    ### 디자이너의 예약 목록을 무한 스크롤로 조회하는 API입니다. \n
                    \n
                    ---\n
                    ### Request Param\n
                    - `month`(optional) : yyyy-MM\n
                    - `type` : UPCOMING / COMPLETED\n
                    - `size`(default=12) : 페이지 사이즈\n
                    - `cursorDate`(optional) : 커서 날짜(yyyy-MM-dd)\n
                    - `cursorTime`(optional) : 커서 시간(HH:mm)\n
                    - `cursorId`(optional) : 커서 예약 ID\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 디자이너 권한만 호출 가능합니다.\n
                    """
    )
    ApiResponse<ReservationScrollResponse<DesignerReservationItem>> getDesignerReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam(required = false) String month,
            @RequestParam ReservationListType type,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime,
            @RequestParam(required = false) Long cursorId
    );

    @Operation(
            summary = "오늘의 예약 조회(디자이너)",
            description = """
                ### 디자이너의 특정 날짜 예약(확정 상태) 목록과 총 개수를 조회하는 API입니다. \n
                - 프론트에서 `date`를 전달하면, 해당 날짜의 **확정된 예약(RESERVATION_CONFIRMED)** 을 시간순으로 반환합니다. \n
                - 예약이 없으면 `totalCount=0`, `reservations=[]`로 반환됩니다. \
                \n
                ---\n
                ### Request Param\n
                - `date` : 조회할 날짜 (yyyy-MM-dd)\n
                \n
                ---\n
                ### Response\n
                - `date` : 조회 날짜\n
                - `totalCount` : 해당 날짜 총 예약 수\n
                - `reservations[]`\n
                  - `reservationId` : 예약 ID\n
                  - `recruitmentId` : 모집글 ID\n
                  - `time` : 예약 시작 시간 (HH:mm)\n
                  - `modelName` : 모델 이름\n
                  - `imageUrl` : 모델 프로필 이미지\n
                  - `subCategory` : 세부 카테고리 표시값 (ex. 펌/커트)\n
                \n
                ---\n
                ✅ 권한\n
                - 디자이너만 조회 가능합니다.\n
                """
    )
    ApiResponse<DesignerDailyReservationResponse> getDesignerDailyReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam LocalDate date
    );

    @Operation(
            summary = "새로운 예약 신청 조회(디자이너) - 무한스크롤",
            description = """
                ### 디자이너의 '새로운 예약 신청' 목록을 무한 스크롤로 조회하는 API입니다. \n
                - 새로운 예약 신청은 **RESERVATION_PENDING** 상태인 예약을 의미합니다. \n
                - 정렬 기준은 **date 오름차순 → startTime 오름차순 → reservationId 오름차순** 입니다. \n
                - 예약이 없으면 `totalCount=0`, `reservations=[]` 로 반환됩니다. \n
                \n
                ---\n
                ### Request Param\n
                - `size` : 페이지 사이즈 (default=12)\n
                - `cursorDate`(optional) : 커서 날짜 (yyyy-MM-dd)\n
                - `cursorTime`(optional) : 커서 시간 (HH:mm)\n
                - `cursorId`(optional) : 커서 예약 ID\n
                \n
                ✅ 커서 규칙\n
                - 최초 호출 시 커서 파라미터를 보내지 않습니다.\n
                - 이후 호출은 응답의 `cursor(cursorDate, cursorTime, cursorId)`를 그대로 다음 요청에 사용합니다.\n
                \n
                ---\n
                ### Response\n
                - `totalCount` : 전체 신규 예약 신청 수(해당 디자이너의 pending 전체)\n
                - `hasNext` : 다음 페이지 존재 여부\n
                - `cursorDate` : 다음 요청 커서 날짜(yyyy-MM-dd)\n
                - `cursorTime` : 다음 요청 커서 시간(HH:mm)\n
                - `cursorId` : 다음 요청 커서 예약 ID\n
                - `reservations[]`\n
                  - `reservationId` : 예약 ID\n
                  - `date` : 예약 날짜 (yyyy-MM-dd)\n
                  - `time` : 예약 시작 시간 (HH:mm)\n
                  - `modelName` : 모델 이름\n
                  - `subCategories` : 세부 카테고리 목록 (ex. ["펌","커트"])\n
                \n
                ---\n
                ✅ 권한\n
                - 디자이너만 조회 가능합니다.\n
                """
    )
    ApiResponse<DesignerPendingReservationScrollResponse> getPendingReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime,
            @RequestParam(required = false) Long cursorId
    );

    @Operation(
            summary = "예약 상세 조회(디자이너)",
            description = """
                ### 디자이너가 예약 상세 화면에 필요한 정보를 조회하는 API입니다. \n
                - reservationId로 예약 1건을 조회합니다. \n
                - **해당 예약의 디자이너 본인만** 조회 가능합니다. \n
                \n
                ---\n
                ### Path Variable\n
                - `reservationId` : 예약 ID\n
                \n
                ---\n
                ### Response\n
                - `reservationId` : 예약 ID\n
                - `status` : 예약 상태 (PENDING/CONFIRMED/CANCELLED ...)\n
                - `date` : 예약 날짜 (yyyy-MM-dd)\n
                - `startTime` / `endTime` : 예약 시간 (HH:mm)\n
                - `category` : 상위 카테고리\n
                - `subCategories` : 하위 카테고리 리스트\n
                - `modelName` : 신청자(모델) 이름\n
                - `imageUrl` : 첨부 이미지 URL\n
                - `comment` : 모델 요청사항\n
                - `cancelReason` : 취소 사유 (취소된 경우 nullable)\n
                \n
                ---\n
                ✅ 권한\n
                - 해당 예약의 디자이너 본인만 조회 가능합니다.\n
                """
    )
    public ApiResponse<DesignerReservationDetailResponse> getDesignerReservationDetail(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId
    );

    @Operation(
            summary = "예약 확정(디자이너)",
            description = """
                ### 디자이너가 '신규 예약 신청(PENDING)'을 확정(CONFIRMED)하는 API입니다. \n
                - **Request Body 없이** 호출합니다.\n
                - 예약 신청 시점에 이미 해당 시간대(RecruitmentTime)가 reserve 처리되어 있으므로,\n
                  확정 시에는 **Reservation 상태만 CONFIRMED로 변경**합니다.\n
                \n
                ---\n
                ### Path Variable\n
                - `reservationId` : 확정할 예약 ID\n
                \n
                ---\n
                ### 처리 조건\n
                - 로그인 사용자가 해당 예약의 디자이너여야 합니다.\n
                - 예약 상태가 **RESERVATION_PENDING** 인 경우에만 확정 가능합니다.\n
                - 예약 시작 시간이 **현재 시간 이후**여야 합니다. (이미 지난 예약은 확정 불가)\n
                \n
                ---\n
                ### 동작\n
                - 예약 시작 시간 검증(now < reservationStartAt)\n
                - Reservation 상태를 **RESERVATION_CONFIRMED**로 변경\n
                \n
                ---\n
                ### Response\n
                - `SimpleMessageDTO` : \"예약이 확정되었습니다.\"\n
                """
    )
    ApiResponse<SimpleMessageDTO> confirmReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId
    );

    @Operation(
            summary = "예약 거절(디자이너)",
            description = """
                ### 디자이너가 '신규 예약 신청(PENDING)'을 거절하는 API입니다. \n
                - **Request Body 없이** 호출합니다. (거절 사유 입력 없음)\n
               
                \n
                ---\n
                ### Path Variable\n
                - `reservationId` : 거절할 예약 ID\n
                \n
                ---\n
                ### 처리 조건\n
                - 로그인 사용자가 해당 예약의 디자이너여야 합니다.\n
                - 예약 상태가 **RESERVATION_PENDING** 인 경우에만 거절 가능합니다.\n
                \n
                ---\n
                ### 동작\n
                - Reservation 상태를 거절 상태로 변경(사유 저장 없음)\n
                \n
                ---\n
                ### Response\n
                - `SimpleMessageDTO` : \"예약이 거절되었습니다.\"\n
                """
    )
    ApiResponse<SimpleMessageDTO> rejectReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId
    );
}
