package modelly.modelly_be.domain.reservation.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DesignerReservationDetailResponse(
        Long reservationId,
        Long recruitmentId,
        String recruitmentTitle,
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
