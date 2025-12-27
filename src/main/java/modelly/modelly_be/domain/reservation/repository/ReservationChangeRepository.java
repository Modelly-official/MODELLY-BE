package modelly.modelly_be.domain.reservation.repository;


import modelly.modelly_be.domain.reservation.entity.ReservationChange;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationChangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationChangeRepository extends JpaRepository<ReservationChange, Long> {

    boolean existsByReservation_IdAndStatus(Long reservationId, ReservationChangeStatus status);
}
