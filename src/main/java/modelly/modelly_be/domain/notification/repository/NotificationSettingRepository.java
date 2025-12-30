package modelly.modelly_be.domain.notification.repository;

import modelly.modelly_be.domain.notification.entity.NotificationSetting;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    Optional<NotificationSetting> findByUser(User user);
}
