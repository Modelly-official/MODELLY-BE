package modelly.modelly_be.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationRemindScheduler {

    private final ReservationRepository reservationRepository;

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void cancelExpiredPendingReservations() {
        log.info("=== 예약 대기 자동 취소 스케줄러 시작 ===");

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<Reservation> expiredReservations =
                reservationRepository.findByStatusAndDateBefore(
                        ReservationStatus.RESERVATION_PENDING,
                        today
                );

        if (expiredReservations.isEmpty()) {
            log.info("자동 취소 대상 예약이 없습니다.");
            return;
        }

        log.info("자동 취소 대상 예약 수: {}", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            reservation.cancel("예약 기간 만료로 자동 취소");
        }

        log.info("=== 예약 대기 자동 취소 완료 ===");
    }
}
