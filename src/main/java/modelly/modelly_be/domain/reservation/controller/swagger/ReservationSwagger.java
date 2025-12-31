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
        name = "예약 공통 API",
        description = "예약 변경 요청, 예약 변경 요청 수락/취소/거절, 예약 기존대로 진행, 예약취소"
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
                    - payload에는 eventType + 안내 문구만 포함합니다.\n
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


}
