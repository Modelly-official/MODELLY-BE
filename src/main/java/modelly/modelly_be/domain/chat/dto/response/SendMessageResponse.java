package modelly.modelly_be.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.entity.ChattingImage;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class SendMessageResponse {
    private Long messageId;
    private Long chatRoomId;
    private Long senderId;
    private MessageType messageType;
    private String message;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    private boolean isRead;

    public static SendMessageResponse of(Chatting chatting, List<ChattingImage> images) {

        List<String> urls = images.stream()
                .map(ChattingImage::getImageUrl)
                .toList();

        return new SendMessageResponse(
                chatting.getId(),
                chatting.getChatRoom().getId(),
                chatting.getSenderId(),
                chatting.getMessageType(),
                chatting.getMessage(),
                urls,
                chatting.getCreatedAt(),
                Boolean.TRUE.equals(chatting.getIsRead())
        );
    }
}
