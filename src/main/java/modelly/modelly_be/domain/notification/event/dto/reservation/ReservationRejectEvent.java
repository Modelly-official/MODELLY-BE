package modelly.modelly_be.domain.notification.event.dto.reservation;

import modelly.modelly_be.domain.reservation.entity.Reservation;

public record ReservationRejectEvent(
        Reservation reservation,
        boolean isNotificationOn
) {
}
