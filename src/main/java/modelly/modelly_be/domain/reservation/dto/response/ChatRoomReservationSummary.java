package modelly.modelly_be.domain.reservation.dto.response;

import java.time.LocalDate;

public record ChatRoomReservationSummary(
        Long recruitmentId,
        String recruitmentTitle,
        LocalDate date,
        String startTime, // HH:mm
        String endTime,   // HH:mm
        Long opponentUserId,
        String opponentName
) {}
