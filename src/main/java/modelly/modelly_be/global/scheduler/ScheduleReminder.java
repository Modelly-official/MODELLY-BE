package modelly.modelly_be.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.notification.service.mapping.ScheduleNotificationService;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleReminder {

    private final ReservationService reservationService;
    private final ScheduleNotificationService scheduleNotificationService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void scheduleRemind(){
        LocalDate oneDaysLater = LocalDate.now().plusDays(1);
        //다음날 스케줄들 예약 id 가져와서 알림전송

        List<Reservation> reservationList = reservationService.getAllByStartDate(oneDaysLater);

        for (Reservation reservation : reservationList){
            try {
                User model = reservation.getModel().getUser();
                User designer = reservation.getDesigner().getUser();

                if (model.getNotificationSetting().isScheduleNotification()){
                    scheduleNotificationService.createRemindNotification(model, reservation);
                }

                if (designer.getNotificationSetting().isScheduleNotification()){
                    scheduleNotificationService.createRemindNotification(designer, reservation);
                }
            } catch (Exception e){
                log.error("❌ 알림 전송 실패", e);
            }
        }
    }
}
