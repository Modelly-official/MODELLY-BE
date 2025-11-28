package modelly.modelly_be.domain.user.repository;

import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DesignerRepository extends JpaRepository<Designer, Long> {
    boolean existsByUser_Id(Long userId);

    Optional<Designer> findByUser_Id(Long userId);

    Optional<Designer> findByUser(User user);
}
