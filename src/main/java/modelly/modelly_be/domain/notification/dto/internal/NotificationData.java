package modelly.modelly_be.domain.notification.dto.internal;

import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.user.entity.User;

public record NotificationData(
        Long userId,
        NotificationType type,
        String title,
        String message,
        Long targetId,
        String senderName //채팅일 경우
) {

    public static NotificationData chattingNotification(Long userId, NotificationType type, String title, String message,Long chattingRoomId, String senderName) {
        return new NotificationData(
                userId,
                type,
                title,
                message,
                chattingRoomId,
                senderName
        );
    }

    public static NotificationData otherNotification(Long userId, NotificationType type, String title, String message,Long targetId) {
        return new NotificationData(
                userId,
                type,
                title,
                message,
                targetId,
                null
        );
    }
}
