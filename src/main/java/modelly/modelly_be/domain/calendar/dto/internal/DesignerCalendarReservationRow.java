package modelly.modelly_be.domain.calendar.dto.internal;

import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record DesignerCalendarReservationRow(
        Long reservationId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        ReservationStatus status,

        Long recruitmentId,
        String recruitmentTitle,

        Long modelUserId,
        Long modelId,
        String modelName
) {}