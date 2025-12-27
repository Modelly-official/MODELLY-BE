package modelly.modelly_be.domain.reservation.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ReservationScrollResponse<T>(
        List<T> items,
        long totalCount,
        boolean hasNext,
        LocalDate nextCursorDate,
        String nextCursorTime, // "HH:mm"
        Long nextCursorId
) {}