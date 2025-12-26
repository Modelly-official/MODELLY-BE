package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.repository.ReservationRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public void hasPendingOrConfirmedReservation(Recruitment recruitment) {
        List<Reservation> reservations = reservationRepository.findAllByRecruitment(recruitment);

        LocalDateTime now = LocalDateTime.now();

        boolean hasPending = reservations.stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.RESERVATION_PENDING);

        if (hasPending) {
            throw new GeneralException(ErrorStatus.CAN_NOT_RECRUITMENT_DELETE_OR_MODIFY);
        }

        boolean hasFutureConfirmed = reservations.stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.RESERVATION_CONFIRMED
                        && LocalDateTime.of(r.getDate(), r.getStartTime()).isAfter(now));

        if (hasFutureConfirmed) {
            throw new GeneralException(ErrorStatus.CAN_NOT_RECRUITMENT_DELETE_OR_MODIFY);
        }
    }

    @Transactional
    public void deleteRelationshipWithRecruitment(Recruitment recruitment) {
        List<Reservation> reservations = reservationRepository.findAllByRecruitment(recruitment);

        for (Reservation r : reservations) {
            r.deleteRelationShip();
            reservationRepository.save(r);
        }
    }

    public Reservation getById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION));
    }

    // 예약 저장
    @Transactional
    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public boolean existsTimeConflict(Designer designer, LocalDate date, LocalTime start, LocalTime end) {
        return reservationRepository.existsByDesignerAndDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                designer,
                date,
                List.of(ReservationStatus.RESERVATION_CONFIRMED, ReservationStatus.RESERVATION_PENDING),
                end,
                start
        );
    }
}
