package modelly.modelly_be.domain.reservation.repository;


import modelly.modelly_be.domain.reservation.entity.ReservationChange;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationChangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationChangeRepository extends JpaRepository<ReservationChange, Long> {

    boolean existsByReservation_IdAndStatus(Long reservationId, ReservationChangeStatus status);

    // 해당 디자이너의 reservation time이 pending 상태인지 확인
    boolean existsByReservation_Designer_IdAndStatusAndProposedDateAndProposedStartTime(
            Long designerId,
            ReservationChangeStatus status,
            LocalDate date,
            LocalTime startTime
    );

    // Requester(요청자) 혹은 Responder(응답자)가 해당 유저인 경우 모두 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ReservationChange rc WHERE rc.requesterUserId = :userId OR rc.responderUserId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
