package modelly.modelly_be.domain.user.repository;

import modelly.modelly_be.domain.user.entity.Designer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignerRepository extends JpaRepository<Designer, Long> {
    boolean existsByUser_Id(Long userId);
}
