package modelly.modelly_be.domain.chat.dto.response;

public record ReservationSimpleTextPayload(
        String eventType,
        Long reservationChangeId,
        String notice
) {}