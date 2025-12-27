package modelly.modelly_be.domain.chat.dto.response;

import java.time.LocalDate;

public record ReservationCancelChatPayload(
        String eventType,          // "CANCEL"
        Long reservationId,

        LocalDate date,
        String startTime,          // HH:mm
        String endTime,            // HH:mm

        String reason,

        String notice              // "예약이 취소되었어요... "
) {}
