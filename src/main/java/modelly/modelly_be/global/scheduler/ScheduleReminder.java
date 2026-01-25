package modelly.modelly_be.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.service.FCMService;
import modelly.modelly_be.domain.notification.service.NotificationService;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.notification.service.mapping.ScheduleNotificationService;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleReminder {

    private final ReservationService reservationService;
    private final ScheduleNotificationService scheduleNotificationService;
    private final NotificationService notificationService;
    private final FCMService fcmService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    @Transactional
    public void scheduleRemind(){
        LocalDate oneDaysLater = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        //다음날 스케줄들 예약 id 가져와서 알림전송

        List<Reservation> reservationList = reservationService.getAllByDate(oneDaysLater);

        for (Reservation reservation : reservationList){
            try {
                User model = reservation.getModel().getUser();

                NotificationData notificationData = scheduleNotificationService.createRemindNotification(model, reservation);
                notificationService.sendNotification(notificationData);

                if (model.getNotificationSetting().isScheduleNotification()){
                    fcmService.pushToFCM(notificationData);
                }

            } catch (Exception e){
                log.error("❌ 알림 전송 실패", e);
            }

            try {
                User designer = reservation.getDesigner().getUser();

                NotificationData notificationData = scheduleNotificationService.createRemindNotification(designer, reservation);
                notificationService.sendNotification(notificationData);

                if (designer.getNotificationSetting().isScheduleNotification()){
                    fcmService.pushToFCM(notificationData);
                }

            } catch (Exception e){
                log.error("❌ 알림 전송 실패", e);
            }
        }
    }
}
