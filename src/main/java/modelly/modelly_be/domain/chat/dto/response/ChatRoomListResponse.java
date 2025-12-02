package modelly.modelly_be.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;
import modelly.modelly_be.domain.user.entity.enums.UserRole;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomListResponse {
    private Long roomId;

    private Long otherUserId;
    private String name;
    private String profileImageUrl;
    MessageType messageType;
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    private UserRole role; // DESIGNER or MODEL

    public static ChatRoomListResponse of(
            ChatRoom room,
            Long otherUserId,
            String name,
            String profileImageUrl,
            MessageType messageType,
            Chatting lastChatting,
            UserRole role
    ) {
        return ChatRoomListResponse.builder()
                .roomId(room.getId())
                .otherUserId(otherUserId)
                .name(name)
                .profileImageUrl(profileImageUrl)
                .messageType(messageType)
                .lastMessage(lastChatting != null ? lastChatting.getMessage() : null)
                .lastMessageTime(lastChatting != null ? lastChatting.getCreatedAt() : null)
                .role(role)
                .build();
    }
}
