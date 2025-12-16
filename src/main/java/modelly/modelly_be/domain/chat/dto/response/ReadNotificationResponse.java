package modelly.modelly_be.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;

@Getter
@AllArgsConstructor
public class ReadNotificationResponse {
    /* STOMP에서 읽음 여부 전송할 때 쓰이는 DTO */
    private Long chatRoomId;
    private MessageType messageType;
    private Long readerUserId;      // 누가 읽었는지
    private Long lastReadMessageId; // 이 메시지까지 읽었다
}
