package modelly.modelly_be.domain.notification.service.mapping;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatNotificationService {

    public NotificationData createChattingNotification(MessageType messageType, String chattingMessage, User otherUser, ChatRoom room) {
        String title ="";
        String message = messageType == MessageType.TEXT ? chattingMessage : "사진을 보냈습니다.";

        switch (otherUser.getUserRole()){
            case MODEL -> title = room.getDesigner().getNickname();
            case DESIGNER -> title = room.getModel().getNickname();
        }

        return NotificationData.chattingNotification(otherUser.getId(), NotificationType.CHATTING, title, message, room.getId(), title);
    }
}
