package modelly.modelly_be.domain.notification.dto.response;

public record UnreadNotificationResponse(
        boolean isAllRead,
        int unreadCount
) {
}
