package modelly.modelly_be.domain.notification.repository.notificationRepository;

import modelly.modelly_be.domain.notification.entity.Notification;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.isRead = true where n.user.id = :userId and n.isRead = false")
    void markAllAsRead(Long userId);

    @Query("SELECT COUNT(n) FROM Notification n " +
            "WHERE n.user = :user AND n.isRead = false")
    int countAllByUserAndRead(User user);

    Long countByUserAndNotificationType(User user, NotificationType notificationType);
}
