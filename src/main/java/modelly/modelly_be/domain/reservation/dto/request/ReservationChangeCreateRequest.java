package modelly.modelly_be.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationChangeCreateRequest(
        @NotNull LocalDate proposedDate,     // 2025-12-13
        @NotBlank String proposedStartTime,  // "13:30"
        @NotNull String reason
) {}
