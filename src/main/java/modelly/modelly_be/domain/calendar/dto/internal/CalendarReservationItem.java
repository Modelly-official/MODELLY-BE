package modelly.modelly_be.domain.calendar.dto.internal;

import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;

import java.time.LocalDate;
import java.util.List;

public record CalendarReservationItem(
        Long reservationId,
        Long recruitmentId,

        Long modelUserId,
        Long modelId,
        String modelName,

        List<String> subCategories,

        LocalDate date,
        String startTime, // HH:mm
        String endTime,  // HH:mm

        ReservationListType reservationStatus
) {}
