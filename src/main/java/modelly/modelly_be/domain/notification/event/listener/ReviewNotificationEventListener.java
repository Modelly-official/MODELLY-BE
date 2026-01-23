package modelly.modelly_be.domain.notification.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.event.dto.review.ReplyCreateEvent;
import modelly.modelly_be.domain.notification.event.dto.review.ReviewCreateEvent;
import modelly.modelly_be.domain.notification.service.FCMService;
import modelly.modelly_be.domain.notification.service.NotificationService;
import modelly.modelly_be.domain.notification.service.mapping.ReviewNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewNotificationEventListener {

    private final ReviewNotificationService reviewNotificationService;
    private final NotificationService notificationService;
    private final FCMService fcmService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCreate(ReviewCreateEvent event) {
        NotificationData data = reviewNotificationService.createReviewNotification(
                event.user(),
                event.userNickname(),
                event.reviewId()
        );
        notificationService.sendNotification(data);

        if (event.isNotificationOn()){
            fcmService.pushToFCM(data);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReplyCreate(ReplyCreateEvent event) {
        NotificationData data = reviewNotificationService.createReplyNotification(
                event.user(),
                event.userNickname(),
                event.reviewId()
        );
        notificationService.sendNotification(data);

        if (event.isNotificationOn()){
            fcmService.pushToFCM(data);
        }
    }
}
