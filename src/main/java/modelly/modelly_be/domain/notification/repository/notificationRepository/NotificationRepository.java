package modelly.modelly_be.domain.notification.repository.notificationRepository;

import modelly.modelly_be.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    @Modifying
    @Query("update Notification n set n.isRead = true where n.user.id = :userId")
    void markAllAsRead(Long userId);
}
