package modelly.modelly_be.domain.chat.dto.response;

import java.time.LocalDate;

public record ReservationChangeCancelChatPayload(
        String eventType,
        Long reservationChangeId,
        Long reservationId,
        LocalDate date,
        String startTime,
        String endTime,
        String message
) {}