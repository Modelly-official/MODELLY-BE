package modelly.modelly_be.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.user.entity.enums.UserRole;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomListResponse {
    private Long roomId;

    private Long otherUserId;
    private String name;
    private String profileImageUrl;

    private String lastMessage;
    private LocalDateTime lastMessageTime;

    private UserRole role; // DESIGNER or MODEL

    public static ChatRoomListResponse of(
            ChatRoom room,
            Long otherUserId,
            String name,
            String profileImageUrl,
            Chatting lastChatting,
            UserRole role
    ) {
        return ChatRoomListResponse.builder()
                .roomId(room.getId())
                .otherUserId(otherUserId)
                .name(name)
                .profileImageUrl(profileImageUrl)
                .lastMessage(lastChatting != null ? lastChatting.getMessage() : null)
                .lastMessageTime(lastChatting != null ? lastChatting.getCreatedAt() : null)
                .role(role)
                .build();
    }
}
