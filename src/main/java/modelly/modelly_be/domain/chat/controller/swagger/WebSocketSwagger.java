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

                🔐 인증 (중요)
                - WebSocket CONNECT 시 JWT를 함께 전송해야 합니다.
                - 예시:
                    Authorization: Bearer {accessToken}

                🔌 WebSocket 연결 URL
                - wss://{host}/api/ws/chat

                📌 Publish / Subscribe destination
                - Publish:   /pub/chat/rooms/{roomId}
                             /pub/chat/rooms/{roomId}/read
                - Subscribe: /sub/chat/rooms/{roomId}
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
                    1) 먼저 REST API로 업로드:
                       POST /api/chat/rooms/{roomId}/images  
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
}