package modelly.modelly_be.domain.calendar.dto.internal;

import java.time.LocalDate;

public record CalendarReservationDotItem(
        LocalDate date,
        boolean hasReserved
) {}
