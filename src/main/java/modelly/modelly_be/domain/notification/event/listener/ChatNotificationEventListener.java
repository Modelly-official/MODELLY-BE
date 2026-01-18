package modelly.modelly_be.domain.notification.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.event.dto.chat.ChatEvent;
import modelly.modelly_be.domain.notification.service.FCMService;
import modelly.modelly_be.domain.notification.service.NotificationService;
import modelly.modelly_be.domain.notification.service.mapping.ChatNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatNotificationEventListener {

    private final ChatNotificationService chatNotificationService;
    private final NotificationService notificationService;
    private final FCMService fcmService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatting(ChatEvent event){
        NotificationData data = chatNotificationService.createChattingNotification(
                event.messageType(),
                event.chattingMessage(),
                event.otherUser(),
                event.room()
        );

        notificationService.sendNotification(data);

        fcmService.pushToFCM(data);
    }
}
