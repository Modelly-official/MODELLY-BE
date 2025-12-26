package modelly.modelly_be.domain.reservation.repository;

import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.user.entity.Designer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsReservationByRecruitmentAndStatus(Recruitment recruitment, ReservationStatus reservationStatus);

    void deleteAllByRecruitment(Recruitment recruitment);

    List<Reservation> findAllByRecruitment(Recruitment recruitment);

    // 동일 시간대 예약 확인(예약 상태가 PENDING, CONFIRMED인 경우만 고려)
    boolean existsByDesignerAndDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Designer designer,
            LocalDate date,
            Collection<ReservationStatus> statuses,
            LocalTime end,
            LocalTime start
    );
}
