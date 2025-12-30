package modelly.modelly_be.domain.reservation.dto.response;

import modelly.modelly_be.domain.reservation.dto.internal.DesignerDailyReservationItem;

import java.time.LocalDate;
import java.util.List;

public record DesignerDailyReservationResponse(
        LocalDate date,
        int totalCount,
        List<DesignerDailyReservationItem> reservations
) {
    public static DesignerDailyReservationResponse of(LocalDate date, List<DesignerDailyReservationItem> items) {
        return new DesignerDailyReservationResponse(date, items.size(), items);
    }
}
