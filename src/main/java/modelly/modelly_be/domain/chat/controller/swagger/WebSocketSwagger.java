package modelly.modelly_be.domain.chat.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.chat.dto.request.ReadUpToRequest;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.dto.response.ReadNotificationResponse;
import modelly.modelly_be.domain.chat.dto.response.SendMessageResponse;

@Tag(
        name = "채팅 WebSocket API(Docs)",
        description = """
                STOMP WebSocket을 통해 채팅 기능을 사용할 때 필요한 프로토콜 문서입니다.
                이 엔드포인트들은 Swagger 문서 전용이며 실제 동작은 WebSocket/STOMP로만 이루어집니다.
                """
)
public interface WebSocketSwagger {

    @Operation(
            summary = "채팅 메시지 발송 (STOMP)",
            description = """
                    채팅 메시지를 STOMP WebSocket으로 전송할 때 사용하는 요청 형식입니다.

                    🔌 WebSocket URL
                    - wss://{host}/api/ws/chat

                    📮 Publish
                    - /pub/chat/rooms/{roomId}

                    📥 Subscribe
                    - /sub/chat/rooms/{roomId}

                    🔐 인증
                    - WebSocket CONNECT 시 Authorization 헤더에 JWT 포함

                    📝 예시 요청 (텍스트)
                    {
                      "messageType": "TEXT",
                      "message": "안녕하세요!",
                      "imageUrls": null
                    }

                    📝 예시 요청 (이미지)
                    1) POST /presigned-url/chats/{roomID} 를 통해 s3에 이미지 업로드
                    2) 응답받은 S3 URL 배열을 imageUrls에 포함해 전송

                    {
                      "messageType": "IMAGE",
                      "imageUrls": ["https://s3.../img1.png"],
                      "message": null
                    }
                    """
    )
    SendMessageResponse docSendMessage(SendMessageRequest request);

    @Operation(
            summary = "메시지 읽음 처리 (STOMP)",
            description = """
                    특정 메시지까지 읽었음을 STOMP로 서버에 알려줄 때 사용하는 형식입니다.

                    📮 Publish
                    - /pub/chat/rooms/{roomId}/read

                    📥 Subscribe
                    - /sub/chat/rooms/{roomId}

                    🔐 인증
                    - WebSocket CONNECT 시 Authorization 헤더에 JWT 포함

                    📝 예시 요청
                    { "lastMessageId": 123 }

                    📝 예시 응답
                    {
                      "chatRoomId": 1,
                      "messageType": "READ",
                      "readerUserId": 10,
                      "lastReadMessageId": 123
                    }
                    """
    )
    ReadNotificationResponse docRead(ReadUpToRequest request);

    @Operation(
            summary = "예약 메시지 공통 규칙",
            description = """
                    ✅ 서버 발송 방향(클라이언트는 이 메시지를 '보내는' 게 아니라 '받는' 것만 합니다)\n
                    - Subscribe: /sub/chat/rooms/{roomId}\n
                    \n
                    ✅ 파싱 규칙(프론트)\n
                    - messageType == "RESERVATION"  → message는 JSON 문자열(payload) 입니다. JSON.parse 후 eventType으로 분기하세요.\n
                    - messageType == "TEXT"         → message는 일반 텍스트입니다.\n
                    \n
                    ✅ 참고\n
                    - 아래 예시들은 SendMessageResponse 중 'message' 필드에 들어가는 내용(payload) 포맷을 설명합니다.\n
                    - SendMessageResponse의 나머지 필드(senderUserId, createdAt 등)는 일반 채팅 메시지와 동일하게 내려옵니다.\n
                    """
    )
    SendMessageResponse docReservationMessageRule();

    @Operation(
            summary = "[RESERVATION] 예약 변경 요청 메시지 (eventType=CHANGE_REQUEST)",
            description = """
                    예약 변경 요청 생성 시, 서버가 채팅방에 아래 포맷으로 RESERVATION 메시지를 브로드캐스트합니다.\n
                    \n
                    📥 Subscribe\n
                    - /sub/chat/rooms/{roomId}\n
                    \n
                    ✅ messageType\n
                    - RESERVATION\n
                    \n
                    ✅ message(payload) JSON 스키마\n
                    - eventType: "CHANGE_REQUEST"\n
                    - reservationChangeId: Long\n
                    - reservationId: Long\n
                    - oldDate: "yyyy-MM-dd"\n
                    - oldStartTime: "HH:mm"\n
                    - oldEndTime: "HH:mm"\n
                    - newDate: "yyyy-MM-dd"\n
                    - newStartTime: "HH:mm"\n
                    - newEndTime: "HH:mm"\n
                    - reason: String\n
                    \n
                    📝 예시 payload(JSON)\n
                    {\n
                      "eventType": "CHANGE_REQUEST",\n
                      "reservationChangeId": 10,\n
                      "reservationId": 3,\n
                      "oldDate": "2025-12-30",\n
                      "oldStartTime": "13:00",\n
                      "oldEndTime": "13:30",\n
                      "newDate": "2026-01-02",\n
                      "newStartTime": "15:00",\n
                      "newEndTime": "15:30",\n
                      "reason": "시간 변경 부탁드려요"\n
                    }\n
                    """
    )
    SendMessageResponse docChangeRequestMessage();

    @Operation(
            summary = "[TEXT] 예약 변경 요청 수락 안내 메시지",
            description = """
                    예약 변경 요청 수락 시, 토글 UI가 필요 없어서 TEXT 메시지로 안내 문구만 내려갑니다.\n
                    \n
                    📥 Subscribe\n
                    - /sub/chat/rooms/{roomId}\n
                    \n
                    ✅ messageType\n
                    - TEXT\n
                    \n
                    ✅ message(plain text)\n
                    - 예: "예약 일정 변경 요청이 수락되었습니다. 변경 일정은 예약 내역에서 확인하실 수 있습니다."\n
                    """
    )
    SendMessageResponse docChangeAcceptTextMessage();

    @Operation(
            summary = "[RESERVATION] 예약 변경 요청 취소 메시지 (eventType=CHANGE_CANCEL)",
            description = """
                    예약 변경 요청(PENDING)을 요청자가 취소하면, 서버가 RESERVATION 메시지를 브로드캐스트합니다.\n
                    \n
                    ✅ messageType\n
                    - RESERVATION\n
                    \n
                    ✅ message(payload) JSON 스키마\n
                    - eventType: "CHANGE_CANCEL"\n
                    - reservationChangeId: Long\n
                    - reservationId: Long\n
                    - date: "yyyy-MM-dd"            (기존 예약 일정)\n
                    - startTime: "HH:mm"\n
                    - endTime: "HH:mm"\n
                    - notice: String               (안내 문구)\n
                    \n
                    📝 예시 payload(JSON)\n
                    {\n
                      "eventType": "CHANGE_CANCEL",\n
                      "reservationChangeId": 10,\n
                      "reservationId": 3,\n
                      "date": "2025-12-30",\n
                      "startTime": "13:00",\n
                      "endTime": "13:30",\n
                      "notice": "변경 요청이 취소되었습니다."\n
                    }\n
                    """
    )
    SendMessageResponse docChangeCancelMessage();

    @Operation(
            summary = "[RESERVATION] 예약 변경 요청 거절 메시지 (eventType=CHANGE_REJECTED)",
            description = """
                    예약 변경 요청(PENDING)을 상대방이 거절하면, 프론트에서 토글 UI(기존대로 진행/예약 취소)가 떠야 하므로\n
                    서버는 RESERVATION + JSON payload로 브로드캐스트합니다.\n
                    \n
                    ✅ messageType\n
                    - RESERVATION\n
                    \n
                    ✅ message(payload) JSON 스키마\n
                    - eventType: "CHANGE_REJECTED"\n
                    - reservationChangeId: Long\n
                    - notice: String (안내 문구)\n
                    \n
                    📝 예시 payload(JSON)\n
                    {\n
                      "eventType": "CHANGE_REJECTED",\n
                      "reservationChangeId": 10,\n
                      "notice": "예약 일정 변경 요청이 거절되었습니다. 기존 예약 일정 진행 여부를 선택해주세요."\n
                    }\n
                    """
    )
    SendMessageResponse docChangeRejectedMessage();

    @Operation(
            summary = "[RESERVATION] 기존대로 진행 확정 메시지 (eventType=CHANGE_PROCEED)",
            description = """
                    변경 요청이 거절된 이후, 변경 요청자가 '기존대로 진행'을 선택하면 RESERVATION 메시지가 내려갑니다.\n
                    \n
                    ✅ messageType\n
                    - RESERVATION\n
                    \n
                    ✅ message(payload) JSON 스키마\n
                    - eventType: "CHANGE_PROCEED"\n
                    - reservationChangeId: Long\n
                    - reservationId: Long\n
                    - date: "yyyy-MM-dd"\n
                    - startTime: "HH:mm"\n
                    - endTime: "HH:mm"\n
                    - notice: String\n
                    \n
                    📝 예시 payload(JSON)\n
                    {\n
                      "eventType": "CHANGE_PROCEED",\n
                      "reservationChangeId": 10,\n
                      "reservationId": 3,\n
                      "date": "2025-12-30",\n
                      "startTime": "13:00",\n
                      "endTime": "13:30",\n
                      "notice": "변경 없이 기존 예약 일정으로 진행합니다."\n
                    }\n
                    """
    )
    SendMessageResponse docProceedMessage();

    @Operation(
            summary = "[RESERVATION] 예약 취소 메시지 (eventType=RESERVATION_CANCEL)",
            description = """
                    예약 취소 시, 서버가 RESERVATION 메시지를 브로드캐스트합니다.\n
                    \n
                    ✅ messageType\n
                    - RESERVATION\n
                    \n
                    ✅ message(payload) JSON 스키마\n
                    - eventType: "RESERVATION_CANCEL"\n
                    - reservationId: Long\n
                    - date: "yyyy-MM-dd"\n
                    - startTime: "HH:mm"\n
                    - endTime: "HH:mm"\n
                    - reason: String   (취소 사유)\n
                    - notice: String   (안내 문구)\n
                    \n
                    📝 예시 payload(JSON)\n
                    {\n
                      "eventType": "RESERVATION_CANCEL",\n
                      "reservationId": 3,\n
                      "date": "2025-12-30",\n
                      "startTime": "13:00",\n
                      "endTime": "13:30",\n
                      "reason": "개인 사정으로 취소합니다",\n
                      "notice": "예약이 취소되었어요.\\n해당 시간대에 다시 예약 신청을 받을 수 있습니다."\n
                    }\n
                    """
    )
    SendMessageResponse docReservationCancelMessage();
}