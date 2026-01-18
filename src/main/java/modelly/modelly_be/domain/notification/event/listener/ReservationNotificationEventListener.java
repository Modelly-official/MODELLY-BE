package modelly.modelly_be.domain.notification.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.event.dto.reservation.ReservationAcceptEvent;
import modelly.modelly_be.domain.notification.event.dto.reservation.ReservationCreatedEvent;
import modelly.modelly_be.domain.notification.event.dto.reservation.ReservationRejectEvent;
import modelly.modelly_be.domain.notification.service.FCMService;
import modelly.modelly_be.domain.notification.service.NotificationService;
import modelly.modelly_be.domain.notification.service.mapping.ReservationNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationNotificationEventListener {

    private final ReservationNotificationService messageFactory;
    private final NotificationService notificationService;
    private final FCMService fcmService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCreated(ReservationCreatedEvent event) {
        NotificationData data = messageFactory.createReservationRequestNotification(event.reservation());

        notificationService.sendNotification(data);

        fcmService.pushToFCM(data);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationAccepted(ReservationAcceptEvent event) {
        NotificationData data = messageFactory.createReservationAcceptedNotification(event.reservation());

        notificationService.sendNotification(data);

        fcmService.pushToFCM(data);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationRejected(ReservationRejectEvent event) {
        NotificationData data = messageFactory.createReservationRejectNotification(event.reservation());

        notificationService.sendNotification(data);

        fcmService.pushToFCM(data);
    }


}
