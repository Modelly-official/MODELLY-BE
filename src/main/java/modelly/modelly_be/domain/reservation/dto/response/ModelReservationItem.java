package modelly.modelly_be.domain.reservation.dto.response;

import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

public record ModelReservationItem(
        Long reservationId,

        Long recruitmentId,
        String recruitmentTitle,

        Long designerUserId,
        Long designerId,
        String designerNickname,
        String shop,

        String category,
        List<String> subCategories,

        LocalDate date,
        String startTime,  // "HH:mm"
        String endTime,    // "HH:mm"
        ReservationStatus status
) {}
