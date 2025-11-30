package modelly.modelly_be.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import modelly.modelly_be.domain.chat.entity.Chatting;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private Long messageId;
    private Long senderUserId;
    private String message;
    private LocalDateTime createdAt;

    public static ChatMessageResponse of(Chatting chatting, Long currentUserId) {
        return ChatMessageResponse.builder()
                .messageId(chatting.getId())
                .senderUserId(chatting.getSenderId())
                .message(chatting.getMessage())
                .createdAt(chatting.getCreatedAt())
                .build();
    }
}
