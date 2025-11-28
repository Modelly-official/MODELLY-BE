package modelly.modelly_be.domain.reservation.repository;

import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsReservationByRecruitmentAndStatus(Recruitment recruitment, ReservationStatus reservationStatus);

    void deleteAllByRecruitment(Recruitment recruitment);

    List<Reservation> findAllByRecruitment(Recruitment recruitment);
}
