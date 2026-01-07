package modelly.modelly_be.domain.notification.repository;

import modelly.modelly_be.domain.notification.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    @Query("SELECT f.token " +
            "FROM FcmToken f " +
            "WHERE f.userId = :userId ")
    Optional<String> findByUserId(Long userId);
}
