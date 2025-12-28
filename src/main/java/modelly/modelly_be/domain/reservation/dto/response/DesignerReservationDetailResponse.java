package modelly.modelly_be.domain.reservation.dto.response;

import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;

import java.time.LocalDate;
import java.util.List;

public record DesignerReservationDetailResponse(
        Long reservationId,
        String status,

        LocalDate date,
        String startTime,
        String endTime,

        String category,
        List<String> subCategories,

        Long modelUserId,
        String modelName,

        String imageUrl,
        String comment,

        String cancelReason  // nullable

) {}
