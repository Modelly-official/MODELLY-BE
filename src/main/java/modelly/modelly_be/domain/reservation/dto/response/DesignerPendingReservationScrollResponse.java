package modelly.modelly_be.domain.reservation.dto.response;

import modelly.modelly_be.domain.reservation.dto.internal.DesignerPendingReservationItem;

import java.time.LocalDate;
import java.util.List;

public record DesignerPendingReservationScrollResponse(
        int totalCount, // 전체 신규 신청 수(해당 디자이너의 pending 전체)
        boolean hasNext,
        List<DesignerPendingReservationItem> reservations,

        // cursor
        LocalDate cursorDate,
        String cursorTime,
        Long cursorId
) {}

