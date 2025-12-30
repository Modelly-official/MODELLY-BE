package modelly.modelly_be.domain.user.repository.designerRepository;

import jakarta.persistence.LockModeType;
import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.utils.Coordinate;
import modelly.modelly_be.global.utils.SearchCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DesignerRepository extends JpaRepository<Designer, Long>, DesignerRepositoryCustom {
    boolean existsByUser_Id(Long userId);

    Optional<Designer> findByUser_Id(Long userId);

    Optional<Designer> findByUser(User user);

    // 특정 designer를 Lock으로 잠그고 가져오기
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Designer d where d.id = :id")
    Designer findByIdForUpdate(@Param("id") Long id);

}
