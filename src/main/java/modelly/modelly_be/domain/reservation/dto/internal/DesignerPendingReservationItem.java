package modelly.modelly_be.domain.reservation.dto.internal;

import java.time.LocalDate;
import java.util.List;

public record DesignerPendingReservationItem(
        Long reservationId,
        String recruitmentTitle,
        LocalDate date,
        String time,              // HH:mm
        String modelName,
        List<String> subCategories
) {}
