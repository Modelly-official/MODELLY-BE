package modelly.modelly_be.domain.user.repository.designerRepository;

import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.utils.Coordinate;
import modelly.modelly_be.global.utils.SearchCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DesignerRepository extends JpaRepository<Designer, Long>, DesignerRepositoryCustom {
    boolean existsByUser_Id(Long userId);

    Optional<Designer> findByUser_Id(Long userId);

    Optional<Designer> findByUser(User user);


}
