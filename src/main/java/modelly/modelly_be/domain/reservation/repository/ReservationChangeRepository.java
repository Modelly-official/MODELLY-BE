package modelly.modelly_be.domain.reservation.repository;


import modelly.modelly_be.domain.reservation.entity.ReservationChange;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationChangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationChangeRepository extends JpaRepository<ReservationChange, Long> {

    boolean existsByReservation_IdAndStatus(Long reservationId, ReservationChangeStatus status);

    // 해당 디자이너의 reservation time이 pending 상태인지 확인
    @Query("""
        select (count(rc) > 0)
        from ReservationChange rc
        join rc.reservation r
        where r.designer.id = :designerId
          and rc.status = :status
          and rc.proposedDate = :date
          and rc.proposedStartTime = :startTime
    """)
    boolean existsPendingOnSlot(
            @Param("designerId") Long designerId,
            @Param("status") ReservationChangeStatus status,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime
    );
}
