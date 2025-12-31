package modelly.modelly_be.domain.calendar.dto.response;

import modelly.modelly_be.domain.calendar.dto.internal.CalendarReservationItem;

import java.util.List;

public record CalendarReservationResponse(
        List<CalendarReservationItem> items,
        long totalCount
) {}