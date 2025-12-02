package modelly.modelly_be.domain.user.repository;

import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {
    boolean existsByUser_Id(Long userId);

    Optional<Model> findByUser_Id(Long userId);

    Optional<Model> findByUser(User user);
}
