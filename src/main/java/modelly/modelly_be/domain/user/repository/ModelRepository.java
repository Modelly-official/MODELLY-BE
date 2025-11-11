package modelly.modelly_be.domain.user.repository;

import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Long> {
    boolean existsByUserId(Long userId);
}
