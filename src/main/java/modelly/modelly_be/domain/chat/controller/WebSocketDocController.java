package modelly.modelly_be.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.dto.response.SendMessageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "채팅 WebSocket API(Docs)")
@RestController
@RequestMapping("/docs/chat")
public class WebSocketDocController {

    @Operation(
            summary = "채팅 메세지 발송 (WebSocket)",
            description = """
        STOMP WebSocket을 통해 메시지 전송하는 방법 설명용 엔드포인트입니다.(실제 사용 API X)

        - 연결 URL: ws://{host}/ws/chat
        - publish destination: /pub/chat/rooms/{roomId}
        - subscribe destination: /sub/chat/rooms/{roomId}

        실제로는 HTTP 호출 대신 WebSocket/STOMP로 요청해야 합니다!
        """
    )
    @PostMapping("/send-message")
    public SendMessageResponse docSendMessage(@RequestBody SendMessageRequest request) {
        throw new UnsupportedOperationException("문서용 엔드포인트입니다.");
    }
}
