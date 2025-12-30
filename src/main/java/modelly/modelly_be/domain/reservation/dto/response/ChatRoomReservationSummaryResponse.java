package modelly.modelly_be.domain.reservation.dto.response;

import modelly.modelly_be.domain.reservation.dto.internal.ChatRoomReservationSummary;

import java.util.Optional;

public record ChatRoomReservationSummaryResponse(
        boolean hasReservation,
        ChatRoomReservationSummary reservation
) {}
