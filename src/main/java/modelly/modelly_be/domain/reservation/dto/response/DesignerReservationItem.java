package modelly.modelly_be.domain.reservation.dto.response;

import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.global.entity.SubCategory;

import java.time.LocalDate;
import java.util.List;

public record DesignerReservationItem(
        Long reservationId,
        Long recruitmentId,
        String recruitmentTitle,

        Long modelUserId,
        Long modelId,
        String modelName,

        List<String> subCategories,

        LocalDate date,
        String startTime, // HH:mm
        String endTime,   // HH:mm
        ReservationStatus status
) {}
