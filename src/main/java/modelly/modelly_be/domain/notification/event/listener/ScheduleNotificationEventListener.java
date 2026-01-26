package modelly.modelly_be.domain.notification.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.event.dto.schedule.ScheduleCancelEvent;
import modelly.modelly_be.domain.notification.event.dto.schedule.ScheduleChangeAcceptEvent;
import modelly.modelly_be.domain.notification.event.dto.schedule.ScheduleChangeEvent;
import modelly.modelly_be.domain.notification.event.dto.schedule.ScheduleChangeRejectEvent;
import modelly.modelly_be.domain.notification.service.FCMService;
import modelly.modelly_be.domain.notification.service.NotificationService;
import modelly.modelly_be.domain.notification.service.mapping.ScheduleNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleNotificationEventListener {

    private final ScheduleNotificationService scheduleNotificationService;
    private final NotificationService notificationService;
    private final FCMService fcmService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleChange(ScheduleChangeEvent event) {
        NotificationData data = scheduleNotificationService.createScheduleChangeNotification(
                event.user(),
                event.reservation(),
                event.finalRoomId()
        );
        notificationService.sendNotification(data);

        if (event.isNotificationOn()){
            fcmService.pushToFCM(data);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleCancel(ScheduleCancelEvent event) {

        NotificationData data = scheduleNotificationService.createScheduleCancelNotification(
                event.user(),
                event.reservation(),
                event.finalRoomId()
        );
        notificationService.sendNotification(data);

        if (event.isNotificationOn()){
            fcmService.pushToFCM(data);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleChangeReject(ScheduleChangeRejectEvent event) {
        NotificationData data = scheduleNotificationService.createScheduleChangeRejectNotification(
                event.user(),
                event.reservation(),
                event.finalRoomId()
        );

        notificationService.sendNotification(data);

        if (event.isNotificationOn()){
            fcmService.pushToFCM(data);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleChangeAccept(ScheduleChangeAcceptEvent event) {
        NotificationData data = scheduleNotificationService.createScheduleChangeAcceptNotification(
                event.user(),
                event.reservation(),
                event.finalRoomId()
        );

        notificationService.sendNotification(data);

        if (event.isNotificationOn()){
            fcmService.pushToFCM(data);
        }
    }
}
