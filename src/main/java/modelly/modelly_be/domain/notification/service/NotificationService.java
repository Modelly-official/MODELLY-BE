package modelly.modelly_be.domain.notification.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.dto.response.NotificationListResponse;
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

    @Transactional
    public List<NotificationListResponse> getNotifications(User user, NotificationType notificationType, Long cursorId, int size) {

        //알림들 전부 읽음 처리
        notificationRepository.markAllAsRead(user.getId());

        //카테고리별 조회
        List<NotificationListResponse> notificationList = notificationRepository.findAllByUserAndType(user, notificationType, cursorId, size);

        return notificationList;
    }
}
