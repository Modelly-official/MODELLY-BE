package modelly.modelly_be.domain.reservation.dto.response;

public record ChatRoomReservationSummaryResponse(
        boolean hasReservation,
        ChatRoomReservationSummary reservation
) {}
