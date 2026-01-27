package modelly.modelly_be.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.service.FCMService;
import modelly.modelly_be.domain.notification.service.NotificationService;
import modelly.modelly_be.domain.notification.service.mapping.ReservationNotificationService;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.repository.ReservationRepository;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationRemindScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationNotificationService reservationNotificationService;
    private final NotificationService notificationService;
    private final FCMService fcmService;

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

    //대기중인 예약 리마인드 알림
    @Scheduled(cron = "0 5 9 * * *", zone = "Asia/Seoul")
    @Transactional
    public void remindReservationRequest() {
        log.info("=== 대기중인 예약 리마인드 알림 스케줄러 시작 ===");

        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(seoulZone);

        sendRemindersForDaysPassed(today, 1, "아직 미확정 상태인 신청 건이 있습니다. 지금 바로 확인해보세요!");

        for (int days = 3; days <=7; days++) {
            String title = String.format("신청한 지 %d일이 된 미확정 건이 있습니다. 지금 바로 확인해보세요!", days);
            sendRemindersForDaysPassed(today, days, title);
        }

        for (int daysUntil = 3; daysUntil >=1; daysUntil--) {
            String title = String.format("시술 %d일 전 미확정 건이 있어요. 지금 바로 예약 신청을 확인하세요.", daysUntil);
            sendRemindersForUpcomingReservation(today, daysUntil, title);
        }

        log.info("=== 리마인드 알림 종료 ===");

    }

    //예약 신청 후 N일 경과한 PENDING 상태의 예약에 대한 리마인드 알림 전송
    private void sendRemindersForDaysPassed(LocalDate today, int dayPassed, String title) {
        LocalDate targetDate = today.minusDays(dayPassed);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<Reservation> reservations = reservationRepository.findByStatusAndCreatedAtBetween(
                ReservationStatus.RESERVATION_PENDING,
                startOfDay,
                endOfDay
        );

        if (!reservations.isEmpty()) {
            log.info("[신청 후 {}일 경과] 알림 대상 : {}건", dayPassed, reservations.size());
            sendReminders(reservations, title);
        }
    }

    //시술 예정일 N일 전인 PENDING 상태의 예약에 대한 리마인드 알림 전송
    private void sendRemindersForUpcomingReservation(LocalDate today, int daysUntil, String title) {
        LocalDate targetDate = today.plusDays(daysUntil);

        List<Reservation> reservations = reservationRepository.findByStatusAndDate(
            ReservationStatus.RESERVATION_PENDING,
                targetDate
        );

        if (!reservations.isEmpty()) {
            log.info("[시술 {}일 전] 알림 대상 : {}건", daysUntil, reservations.size());
            sendReminders(reservations, title);
        }
    }

    private void sendReminders(List<Reservation> reservations, String title) {
        if (reservations.isEmpty()) return;

        for (Reservation reservation : reservations) {
            try {
                NotificationData data = reservationNotificationService.createReservationRemindNotification(reservation, title);
                notificationService.sendNotification(data);

                User designer = reservation.getDesigner().getUser();

                if (designer.getNotificationSetting().isReservationNotification()){
                    fcmService.pushToFCM(data);
                }

            } catch (Exception e) {
                log.error("[알림 실패] 예약ID: {}", reservation.getId(), e);
            }
        }
    }
}
