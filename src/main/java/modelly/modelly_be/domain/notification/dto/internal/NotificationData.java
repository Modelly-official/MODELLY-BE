package modelly.modelly_be.domain.notification.dto.internal;

import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.user.entity.User;

public record NotificationData(
        User user,
        NotificationType type,
        String title,
        String message,
        Long targetId,
        String senderName //채팅일 경우
) {

    public static NotificationData chattingNotification(User user, NotificationType type, String title, String message,Long chattingRoomId, String senderName) {
        return new NotificationData(
                user,
                type,
                title,
                message,
                chattingRoomId,
                senderName
        );
    }

    public static NotificationData otherNotification(User user, NotificationType type, String title, String message,Long targetId) {
        return new NotificationData(
                user,
                type,
                title,
                message,
                targetId,
                null
        );
    }
}
