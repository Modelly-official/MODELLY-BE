package modelly.modelly_be.domain.notification.event.dto.review;

import modelly.modelly_be.domain.user.entity.User;

public record ReviewCreateEvent(
        User user,
        String userNickname,
        Long reviewId,
        boolean isNotificationOn
) {
}
