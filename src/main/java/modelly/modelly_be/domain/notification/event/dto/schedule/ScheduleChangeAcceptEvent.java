package modelly.modelly_be.domain.notification.event.dto.schedule;

import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.user.entity.User;

public record ScheduleChangeAcceptEvent(
        User user,
        Reservation reservation,
        Long finalRoomId,
        boolean isNotificationOn
) {
}
