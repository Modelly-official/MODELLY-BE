package modelly.modelly_be.domain.calendar.dto.response;

import modelly.modelly_be.domain.calendar.dto.internal.CalendarReservationDotItem;

import java.util.List;

public record CalendarReservationDotsResponse(
        String month, // yyyy-MM
        List<CalendarReservationDotItem> days
) {}