package modelly.modelly_be.domain.notification.event.dto.reservation;

import modelly.modelly_be.domain.reservation.entity.Reservation;

public record ReservationAcceptEvent(
        Reservation reservation,
        boolean isNotificationOn
) {
}
