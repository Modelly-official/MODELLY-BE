package modelly.modelly_be.domain.chat.controller;

import modelly.modelly_be.domain.chat.controller.swagger.WebSocketSwagger;
import modelly.modelly_be.domain.chat.dto.request.ReadUpToRequest;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.dto.response.ReadNotificationResponse;
import modelly.modelly_be.domain.chat.dto.response.SendMessageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/docs/chat")
public class WebSocketDocController implements WebSocketSwagger {

    @PostMapping("/send-message")
    public SendMessageResponse docSendMessage(@RequestBody SendMessageRequest request) {
        throw new UnsupportedOperationException("문서용 엔드포인트입니다. 실제 메시지 전송은 STOMP WebSocket을 사용하세요.");
    }

    @PostMapping("/read")
    public ReadNotificationResponse docRead(ReadUpToRequest request) {
        throw new UnsupportedOperationException("문서용 엔드포인트입니다. 실제 읽음 처리는 STOMP WebSocket을 사용하세요.");
    }

}
