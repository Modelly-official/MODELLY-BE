package modelly.modelly_be.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationChangeCreateRequest(
        @NotNull(message = "변경 희망 날짜는 필수입니다.")
        LocalDate proposedDate,// 2025-12-13
        @NotBlank(message = "변경 희망 시작 시간은 필수입니다.")
        String proposedStartTime,  // "13:30"
        @NotBlank(message = "변경 사유는 필수입니다.")
        String reason
) {}
