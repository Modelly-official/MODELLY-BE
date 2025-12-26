package modelly.modelly_be.domain.reservation.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvailableReservationScheduleResponse(
        Long recruitmentId,
        String month, // yyyy-MM
        List<DateSchedule> schedules
) {
    public record DateSchedule(
            LocalDate date,
            List<TimeSlot> times
    ) {}

    public record TimeSlot(
            String startTime, // HH:mm
            boolean isReserved
    ) {}
}
