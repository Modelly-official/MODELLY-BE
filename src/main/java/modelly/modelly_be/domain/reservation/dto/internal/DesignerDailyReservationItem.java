package modelly.modelly_be.domain.reservation.dto.internal;

import java.util.List;

public record DesignerDailyReservationItem(
        Long reservationId,
        String time,              // HH:mm
        String modelName,
        List<String> subCategories
) {}