package modelly.modelly_be.domain.reservation.dto.internal;

import java.util.Set;

public record ReservationEditInfoMap(
        Set<Long> pendingRecruitmentIds,
        Set<Long> confirmedRecruitmentIds
) {
    public ReservationEditInfoMap() {
        this(Set.of(), Set.of());
    }
}
