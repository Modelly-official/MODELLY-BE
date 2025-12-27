package modelly.modelly_be.domain.chat.dto.response;

import java.time.LocalDate;

public record ReservationChangeChatPayload(
        String eventType,
        Long reservationChangeId,
        Long reservationId,

        // 기존 일정
        LocalDate oldDate,
        String oldStartTime,
        String oldEndTime,

        // 변경 제안 일정
        LocalDate newDate,
        String newStartTime,
        String newEndTime,

        String reason
) {}
