package modelly.modelly_be.domain.reservation.dto.response;

import modelly.modelly_be.domain.reservation.entity.enums.ReservationChangeStatus;

import java.time.LocalDate;

public record ReservationChangeCreateResponse(
        Long reservationChangeId,
        Long reservationId,
        ReservationChangeStatus status,
        LocalDate proposedDate,
        String proposedStartTime,
        String proposedEndTime,
        String reason
) {}
