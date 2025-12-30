package modelly.modelly_be.domain.calendar.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CalendarReservationScrollResponse(
        List<CalendarReservationItem> items,
        long totalCount,
        boolean hasNext,
        LocalDate nextCursorDate,
        String nextCursorTime, // HH:mm
        Long nextCursorId
) {}