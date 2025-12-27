package modelly.modelly_be.domain.reservation.dto.common;

import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record DesignerReservationRow(
        Long reservationId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        ReservationStatus status,

        Long recruitmentId,
        String recruitmentTitle,

        Long modelId,
        Long modelUserId,
        String modelName
) {}
