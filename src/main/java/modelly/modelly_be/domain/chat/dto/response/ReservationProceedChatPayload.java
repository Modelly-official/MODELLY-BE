package modelly.modelly_be.domain.chat.dto.response;

import java.time.LocalDate;

public record ReservationProceedChatPayload(
        String eventType,           // CHANGE_PROCEED
        Long reservationChangeId,
        Long reservationId,
        LocalDate date,
        String startTime,
        String endTime,
        String notice              // "변경 없이 기존 예약 일정으로 진행합니다."
) {}