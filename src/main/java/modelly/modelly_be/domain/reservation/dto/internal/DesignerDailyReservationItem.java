package modelly.modelly_be.domain.reservation.dto.internal;

import java.util.List;

public record DesignerDailyReservationItem(
        Long reservationId,
        Long recruitmentId,
        String time,              // HH:mm
        String modelName,
        String imageUrl,
        List<String> subCategories
) {}