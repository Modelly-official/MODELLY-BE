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

        //3일 전에 들어온 예약 신청
        // ex) 오늘이 10일이면, 7일에 신청했는데 아직도 PENDING인 것들
        LocalDate createdTargetDate = today.minusDays(3);
        LocalDateTime startOfDay = createdTargetDate.atStartOfDay();
        LocalDateTime endOfDay = createdTargetDate.atTime(LocalTime.MAX);

        List<Reservation> longPendingReservations = reservationRepository.findByStatusAndCreatedAtBetween(
                ReservationStatus.RESERVATION_PENDING,
                startOfDay,
                endOfDay
        );

        //예약 날짜가 2일 남은 예약
        // ex) 오늘이 10일이면, 예약일이 12일인 건들 (D-2)
        LocalDate upcomingReservationDate = today.plusDays(2);

        List<Reservation> imminentReservations = reservationRepository.findByStatusAndDate(
                ReservationStatus.RESERVATION_PENDING,
                upcomingReservationDate
        );

        sendReminders(longPendingReservations, "3일 전 들어온 예약이 있습니다. 예약을 확정해주세요.");
        sendReminders(imminentReservations, "예약일이 3일 남았습니다. 대기 중인 예약을 확인해주세요.");

        log.info("=== 리마인드 알림 종료: 오래된 요청 {}건, 다가오는 요청 {}건 ===",
                longPendingReservations.size(), imminentReservations.size());

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

                log.info("[알림 발송] 예약ID: {}", reservation.getId());
            } catch (Exception e) {
                log.error("[알림 실패] 예약ID: {}", reservation.getId(), e);
            }
        }
    }
}
