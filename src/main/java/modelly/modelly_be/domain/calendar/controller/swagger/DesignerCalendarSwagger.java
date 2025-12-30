package modelly.modelly_be.domain.calendar.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.calendar.dto.response.CalendarReservationDotsResponse;
import modelly.modelly_be.domain.calendar.dto.response.CalendarReservationScrollResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(
        name = "디자이너 캘린더 API",
        description = "디자이너 캘린더 화면에서 사용할 예약 목록 조회(월 전체/특정 날짜) - 무한스크롤"
)
public interface DesignerCalendarSwagger {

    @Operation(
            summary = "캘린더 예약 목록 조회(디자이너) - 무한스크롤 (month 필수, date 선택)",
            description = """
                    ### 디자이너 캘린더 화면에서 사용할 예약 목록을 무한 스크롤로 조회하는 API입니다. \n
                    - `month`는 필수입니다. (yyyy-MM)\n
                    - `date`를 전달하지 않으면, 해당 month의 전체 예약을 정렬하여 반환합니다.\n
                    - `date`를 전달하면, 해당 날짜의 예약만 정렬하여 반환합니다.\n
                    - 정렬 기준은 **date 오름차순 → startTime 오름차순 → reservationId 오름차순** 입니다.\n
                    \n
                    ---\n
                    ### Request Param\n
                    - `month`(required) : 조회할 월 (yyyy-MM)\n
                    - `date`(optional) : 조회할 날짜 (yyyy-MM-dd)\n
                    - `size`(default=20) : 페이지 사이즈\n
                    - `cursorDate`(optional) : 커서 날짜 (yyyy-MM-dd)\n
                    - `cursorTime`(optional) : 커서 시간 (HH:mm)\n
                    - `cursorId`(optional) : 커서 예약 ID\n
                    \n
                    ✅ 커서 규칙\n
                    - 최초 호출 시 커서 파라미터를 보내지 않습니다.\n
                    - 이후 호출은 응답의 `nextCursorDate`, `nextCursorTime`, `nextCursorId`를 다음 요청 커서로 그대로 사용합니다.\n
                    \n
                    ---\n
                    ### Response\n
                    - `items[]`\n
                      - `reservationId` : 예약 ID\n
                      - `recruitmentId` : 공고 ID (nullable)\n
                      - `recruitmentTitle` : 공고 제목 (nullable)\n
                      - `modelUserId` : 모델 userId\n
                      - `modelId` : 모델 id\n
                      - `modelName` : 모델 이름\n
                      - `subCategories` : 시술 목록(문자열 리스트)\n
                      - `date` : 예약 날짜 (yyyy-MM-dd)\n
                      - `startTime` / `endTime` : 예약 시간 (HH:mm)\n
                      - `status` : 예약 상태\n
                    - `totalCount` : 조건에 해당하는 전체 예약 수 (month + date 필터 기준)\n
                    - `hasNext` : 다음 페이지 존재 여부\n
                    - `nextCursorDate` : 다음 요청 커서 날짜\n
                    - `nextCursorTime` : 다음 요청 커서 시간\n
                    - `nextCursorId` : 다음 요청 커서 예약 ID\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 디자이너 권한만 호출 가능합니다.\n
                    """
    )
    ApiResponse<CalendarReservationScrollResponse> getCalendarReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam String month,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime,
            @RequestParam(required = false) Long cursorId
    );

    @Operation(
            summary = "캘린더 도트 조회(디자이너) - 월 전체 날짜 예약 존재 여부",
            description = """
                ### 캘린더에서 도트(예약 존재 여부)를 찍기 위한 API입니다.\n
                - month(yyyy-MM) 기준으로 해당 월의 **모든 날짜**에 대해 `hasReserved` boolean을 내려줍니다.\n
                - 기본은 확정(CONFIRMED) 예약 기준입니다.\n
                - 필요하면 `includePending=true`로 신규 예약 신청(PENDING)도 도트에 포함시킬 수 있습니다.\n
                \n
                ---\n
                ### Request Param\n
                - `month`(required) : yyyy-MM\n
                - `includePending`(optional, default=false) : pending 포함 여부\n
                \n
                ---\n
                ✅ 권한\n
                - 디자이너만 호출 가능합니다.\n
                """
    )
    ApiResponse<CalendarReservationDotsResponse> getReservationDots(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam String month,
            @RequestParam(defaultValue = "false") boolean includePending
    );
}
