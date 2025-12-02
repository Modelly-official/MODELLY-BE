package modelly.modelly_be.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.chat.entity.Chatting;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SendMessageResponse {
    private Long messageId;
    private Long chatRoomId;
    private Long senderId;
    private String message;
    private LocalDateTime createdAt;

    public static SendMessageResponse from(Chatting chatting) {
        return new SendMessageResponse(
                chatting.getId(),
                chatting.getChatRoom().getId(),
                chatting.getSenderId(),
                chatting.getMessage(),
                chatting.getCreatedAt()
        );
    }
}
