package modelly.modelly_be.domain.notification.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.dto.response.NotificationListResponse;
import modelly.modelly_be.domain.notification.entity.Notification;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.notification.repository.notificationRepository.NotificationRepository;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FcmTokenService fcmTokenService;
    private final FCMService fcmService;

    @Transactional
    public List<NotificationListResponse> getNotifications(User user, NotificationType notificationType, Long cursorId, int size) {

        //알림들 전부 읽음 처리
        notificationRepository.markAllAsRead(user.getId());

        //카테고리별 조회
        List<NotificationListResponse> notificationList = notificationRepository.findAllByUserAndType(user, notificationType, cursorId, size);

        return notificationList;
    }

    @Transactional
    public void createNotification(NotificationData data) {
        Notification notification = Notification.builder()
                .user(data.user())
                .notificationType(data.type())
                .targetId(data.targetId())
                .content(data.message())
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        String fcmToken = fcmTokenService.getTokenByUserId(data.user().getId());

        // fcm 전송
        fcmService.pushToFCM(data, fcmToken);
    }

}
