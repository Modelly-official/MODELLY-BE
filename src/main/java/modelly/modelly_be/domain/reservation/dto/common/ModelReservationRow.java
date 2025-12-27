package modelly.modelly_be.domain.reservation.dto.common;

import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ModelReservationRow(
        Long reservationId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        ReservationStatus status,
        Category category,

        Long recruitmentId,
        String recruitmentTitle,

        Long designerUserId,
        Long designerId,
        String designerNickname,
        String shop
) {}
