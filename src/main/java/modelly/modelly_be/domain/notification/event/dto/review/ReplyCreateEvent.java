package modelly.modelly_be.domain.notification.event.dto.review;

import modelly.modelly_be.domain.user.entity.User;

public record ReplyCreateEvent(
        User user,
        String userNickname,
        Long reviewId,
        boolean isNotificationOn
) {
}
