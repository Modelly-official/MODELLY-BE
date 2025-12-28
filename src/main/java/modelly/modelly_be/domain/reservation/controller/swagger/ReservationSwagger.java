package modelly.modelly_be.domain.reservation.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCancelRequest;
import modelly.modelly_be.domain.reservation.dto.request.ReservationChangeCreateRequest;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCreateRequest;
import modelly.modelly_be.domain.reservation.dto.response.*;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(
        name = "예약 관련 API",
        description = "모델/디자이너 예약 관련 API (공통: /reservations, 모델용: /models, 디자이너용: /designers)"
)
public interface ReservationSwagger {

    /* ---------- 모델/디자이너 공통 API ---------- */

    @Operation(
            summary = "예약 변경 요청(공통)",
            description = """
                    ### 확정된 예약(RESERVATION_CONFIRMED)에 대해 일정 변경을 요청하는 API입니다. \n
                    - roomId가 없으면 상대방과의 채팅방을 open(없으면 생성)한 뒤, 예약 변경 요청 메시지를 전송합니다. \n
                    - 변경 요청은 **예약 1건당 PENDING 1건만** 존재할 수 있습니다. \n
                    - 요청 시간대는 디자이너가 공고에서 설정한 시간대(RecruitmentTime)에 존재해야 하고, 비어있어야 합니다. \n
                    \n
                    ---\n
                    ### Path Variable\n
                    - `reservationId` : 변경 요청을 보낼 예약 ID\n
                    \n
                    ---\n
                    ### Query Param\n
                    - `roomId`(optional) : 채팅방 ID (없으면 자동 open)\n
                    \n
                    ---\n
                    ### Request Body\n
                    - `proposedDate` : 변경 희망 날짜 (yyyy-MM-dd)\n
                    - `proposedStartTime` : 변경 희망 시작 시간 (HH:mm)\n
                    - `reason` : 변경 요청 사유\n
                    \n
                    ---\n
                    ### 채팅\n
                    - `MessageType.RESERVATION`으로 전송됩니다.\n
                    - JSON payload(eventType=`CHANGE_REQUEST`) 형태로 전송됩니다.\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 해당 예약의 참가자(모델/디자이너)만 요청할 수 있습니다.\n
                    """
    )
    ApiResponse<SimpleMessageDTO> createReservationChangeRequest(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId,
            @RequestParam(required = false) Long roomId,
            @Valid @RequestBody ReservationChangeCreateRequest request
    );

    @Operation(
            summary = "예약 변경 요청 수락(공통)",
            description = """
                    ### 예약 변경 요청(PENDING)을 수락하여 실제 예약 일정을 변경하는 API입니다. \n
                    - 요청 슬롯(new slot)과 기존 슬롯(old slot)을 검증/갱신하고 예약 일정을 변경합니다. \n
                    \n
                    ---\n
                    ### Path Variable\n
                    - `reservationChangeId` : 예약 변경 요청 ID\n
                    \n
                    ---\n
                    ### 조건\n
                    - 변경 요청은 PENDING 상태여야 합니다.\n
                    - 변경 요청의 requester(요청자)는 수락할 수 없습니다.\n
                    - 원본 예약은 RESERVATION_CONFIRMED 상태여야 합니다.\n
                    \n
                    ---\n
                    ### 동작\n
                    - new slot(요청 슬롯) RecruitmentTime 전역 row들을 락 후 reserved 여부/스케줄 충돌 검증\n
                    - old slot unreserve / new slot reserve\n
                    - Reservation의 date/start/end 반영\n
                    - ReservationChange 상태 ACCEPTED 처리\n
                    \n
                    ---\n
                    ### 채팅\n
                    - 토글 UI가 필요 없으므로 `MessageType.TEXT`로 안내 문구를 전송합니다.\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 해당 예약의 참가자(모델/디자이너)만 가능\n
                    - requester(변경 요청자)는 호출 불가\n
                    """
    )
    ApiResponse<SimpleMessageDTO> acceptReservationChange(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationChangeId
    );

    @Operation(
            summary = "예약 변경 요청 취소(공통)",
            description = """
                    ### 예약 변경 요청(PENDING)을 요청자가 취소하는 API입니다. \n
                    \n
                    ---\n
                    ### Path Variable\n
                    - `reservationChangeId` : 예약 변경 요청 ID\n
                    \n
                    ---\n
                    ### 조건\n
                    - PENDING 상태만 취소 가능\n
                    - 요청자 본인만 취소 가능\n
                    \n
                    ---\n
                    ### 동작\n
                    - ReservationChange 상태를 CANCELED 처리\n
                    \n
                    ---\n
                    ### 채팅\n
                    - `MessageType.RESERVATION` + JSON payload(eventType=`CHANGE_CANCEL`)로 전송합니다.\n
                    - payload에 기존 예약 일정(date/start/end) + 안내 메시지가 포함됩니다.\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - requester(변경 요청자) 본인만 가능\n
                    """
    )
    ApiResponse<SimpleMessageDTO> cancelReservationChange(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationChangeId
    );

    @Operation(
            summary = "예약 변경 요청 거절(공통)",
            description = """
                    ### 예약 변경 요청(PENDING)을 상대방이 거절하는 API입니다. \n
                    - 거절 시 프론트에서 '기존대로 진행' / '예약 취소' 토글 UI가 생성됩니다. \n
                    \n
                    ---\n
                    ### Path Variable\n
                    - `reservationChangeId` : 예약 변경 요청 ID\n
                    \n
                    ---\n
                    ### 조건\n
                    - PENDING 상태만 거절 가능\n
                    - 변경 요청의 requester(요청자)는 거절할 수 없습니다.\n
                    - 원본 예약은 RESERVATION_CONFIRMED 상태여야 합니다.\n
                    \n
                    ---\n
                    ### 동작\n
                    - ReservationChange 상태를 REJECTED 처리\n
                    \n
                    ---\n
                    ### 채팅\n
                    - `MessageType.RESERVATION` + JSON payload(eventType=`CHANGE_REJECTED`)로 전송합니다.\n
                    - payload에는 eventType + 안내 문구(message)만 포함합니다.\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 해당 예약의 참가자(모델/디자이너)만 가능\n
                    - requester(변경 요청자)는 호출 불가\n
                    """
    )
    ApiResponse<SimpleMessageDTO> rejectReservationChange(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationChangeId
    );

    @Operation(
            summary = "예약 취소(공통)",
            description = """
                    ### 예약을 취소하는 API입니다. (확정/대기 예약) \n
                    - 취소 사유를 입력합니다. \n
                    - 예약 시작 시간 기준 **72시간(3일) 전까지만** 취소 가능합니다. \n
                    \n
                    ---\n
                    ### Path Variable\n
                    - `reservationId` : 취소할 예약 ID\n
                    \n
                    ---\n
                    ### Query Param\n
                    - `roomId`(optional) : 채팅방 ID (없으면 자동 open)\n
                    - `reservationChangeId`(optional) : 변경 요청 거절 후 취소 플로우 검증용\n
                    \n
                    ---\n
                    ### Request Body\n
                    - `reason` : 취소 사유\n
                    \n
                    ---\n
                    ### 조건\n
                    - 해당 예약 참가자(모델/디자이너)만 가능\n
                    - 이미 취소(RESERVATION_CANCELLED) 상태면 불가\n
                    - 예약 시작 72시간 미만으로 남았으면 취소 불가\n
                    \n
                    ---\n
                    ### 예약 변경 거절 후 취소(Optional)\n
                    - `reservationChangeId`가 전달된 경우:\n
                      - 해당 change가 이 reservation에 속해야 함\n
                      - change는 REJECTED 상태여야 함\n
                      - change의 requester(변경 요청자)만 취소 가능\n
                    \n
                    ---\n
                    ### 동작\n
                    - RecruitmentTime 전역 슬롯(동일 디자이너/날짜/시작시간) 조회 후 unreserve 처리\n
                    - Reservation 상태를 RESERVATION_CANCELLED로 변경 + cancelReason 저장\n
                    \n
                    ---\n
                    ### 채팅\n
                    - `MessageType.RESERVATION` + JSON payload(eventType=`RESERVATION_CANCEL`) 전송\n
                    - payload: 기존 일정(date/start/end), 취소 사유, 안내 문구 포함\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 해당 예약 참가자만 가능\n
                    """
    )
    ApiResponse<SimpleMessageDTO> cancelReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long reservationChangeId,
            @RequestBody @Valid ReservationCancelRequest request
    );

    @Operation(
            summary = "기존대로 진행(공통)",
            description = """
                    ### 예약 변경 요청이 거절(REJECTED)된 이후, 변경 요청자가 '변경 없이 진행'을 확정하는 API입니다. \n
                    - 실제 Reservation/ReservationChange 상태를 추가로 변경하지 않고, 채팅 메시지만 전송합니다. \n
                    \n
                    ---\n
                    ### Path Variable\n
                    - `reservationChangeId` : 예약 변경 요청 ID\n
                    \n
                    ---\n
                    ### 조건\n
                    - ReservationChange 상태가 REJECTED여야 합니다.\n
                    - 변경 요청자(requester) 본인만 호출 가능합니다.\n
                    \n
                    ---\n
                    ### 채팅\n
                    - `MessageType.RESERVATION` + JSON payload(eventType=`CHANGE_PROCEED`) 전송\n
                    - payload: 기존 일정(date/start/end) + 안내 문구 포함\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - requester(변경 요청자) 본인만 가능\n
                    """
    )
    ApiResponse<SimpleMessageDTO> proceedAsIs(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationChangeId
    );

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
                - 예약이 없으면 `totalCount=0`, `reservations=[]`로 반환됩니다. \n
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
                  - `time` : 예약 시작 시간 (HH:mm)\n
                  - `modelName` : 모델 이름\n
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
}
