package modelly.modelly_be.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.dto.response.NotificationListResponse;
import modelly.modelly_be.domain.notification.dto.response.UnreadNotificationResponse;
import modelly.modelly_be.domain.notification.entity.Notification;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.notification.repository.notificationRepository.NotificationRepository;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.redis.RedisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FcmTokenService fcmTokenService;
    private final FCMService fcmService;
    private final RedisService redisService;

    private static final String UNREAD_COUNT_KEY = "user:unread_notification_count:";

    @Transactional
    public List<NotificationListResponse> getNotifications(User user, NotificationType notificationType, Long cursorId, int size) {

        //카테고리별 조회
        List<NotificationListResponse> notificationList = notificationRepository.findAllByUserAndType(user, notificationType, cursorId, size);

        //알림들 전부 읽음 처리
        notificationRepository.markAllAsRead(user.getId());

        String key = UNREAD_COUNT_KEY + user.getId();
        redisService.setValue(key, 0, 0L);

        return notificationList;
    }

    public void sendNotification(NotificationData data) {
        createNotification(data);

        String fcmToken = fcmTokenService.getToken(data.user().getId());

        // fcm 전송
        if (fcmToken != null) {
            fcmService.pushToFCM(data, fcmToken);
        }

        incrementUnreadCount(data.user());
    }

    @Transactional
    public void createNotification(NotificationData data){
        Notification notification = Notification.builder()
                .user(data.user())
                .notificationType(data.type())
                .targetId(data.targetId())
                .title(data.title())
                .content(data.message())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public UnreadNotificationResponse countUnreadNotification(User user) {

        String key = UNREAD_COUNT_KEY + user.getId();

        int count;
        if (redisService.checkExistsValue(key)){
            count =Integer.parseInt(redisService.getValue(key).toString());
        } else {
            log.info("DB에서 가져옵니다.");
            count = notificationRepository.countAllByUserAndRead(user);
            redisService.setValue(key, count, 0L);
        }

        boolean isAllRead = false;
        if (count == 0) {
            isAllRead = true;
        }

        return new UnreadNotificationResponse(isAllRead, count);
    }

    @Transactional
    public void incrementUnreadCount(User user) {
        String key = UNREAD_COUNT_KEY + user.getId();
        if (redisService.checkExistsValue(key)) {
            redisService.incrementCount(key);
        } else {
            log.info("DB에서 가져옵니다.");
            int count = notificationRepository.countAllByUserAndRead(user);
            redisService.setValue(key, count, 0L);
        }
    }
}
