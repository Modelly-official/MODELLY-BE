package modelly.modelly_be.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.entity.ChattingImage;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatMessageResponse {
    private Long messageId;
    private Long senderUserId;
    private MessageType messageType;
    private String message;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static ChatMessageResponse of(Chatting chatting, List<String> imageUrls) {
        return ChatMessageResponse.builder()
                .messageId(chatting.getId())
                .senderUserId(chatting.getSenderId())
                .messageType(chatting.getMessageType())
                .message(chatting.getMessage())
                .imageUrls(imageUrls)
                .createdAt(chatting.getCreatedAt())
                .build();
    }
}
