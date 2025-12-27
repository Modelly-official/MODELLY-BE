package modelly.modelly_be.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReservationCancelRequest(
        @NotBlank String reason
) {}