package modelly.modelly_be.domain.notification.repository.notificationRepository;

import modelly.modelly_be.domain.notification.dto.response.NotificationListResponse;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.user.entity.User;

import java.util.List;

public interface NotificationRepositoryCustom{

    List<NotificationListResponse> findAllByUserAndType(User user, NotificationType notificationType, Long cursorId, int size);
}
