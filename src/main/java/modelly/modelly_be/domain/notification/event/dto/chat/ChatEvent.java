package modelly.modelly_be.domain.notification.event.dto.chat;

import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;
import modelly.modelly_be.domain.user.entity.User;

public record ChatEvent(
        MessageType messageType,
        String chattingMessage,
        User otherUser,
        ChatRoom room
) {
}
