package modelly.modelly_be.domain.user.repository.designerRepository;

import jakarta.persistence.LockModeType;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.global.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DesignerRepository extends JpaRepository<Designer, Long>, DesignerRepositoryCustom {
    boolean existsByUser_Id(Long userId);

    Optional<Designer> findByUser_Id(Long userId);

    Optional<Designer> findByUser(User user);

    // 특정 designer를 Lock으로 잠그고 가져오기
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Designer d where d.id = :id")
    Designer findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT COUNT(DISTINCT d.id) FROM Designer d " +
            "WHERE d.user.userRole = :userRole AND d.user.deletedAt IS NULL " +
            "AND (:keyword IS NULL OR d.nickname LIKE %:keyword% OR d.shop LIKE %:keyword%) " +
            "AND (:category IS NULL OR d.category = :category) ")
    Long countByCondition(String keyword, Category category, UserRole userRole);

    @Modifying
    @Query("UPDATE Designer d SET d.likeCount = d.likeCount - 1 " +
            "WHERE d.id = :designerId AND d.likeCount > 0")
    void decrementLikeCount(@Param("designerId") Long designerId);

    @Modifying
    @Query("UPDATE Designer d SET d.likeCount = d.likeCount + 1 " +
            "WHERE d.id = :designerId")
    void incrementLikeCount(@Param("designerId") Long designerId);

    @Modifying
    @Query("UPDATE Designer d SET d.reviewCount = d.reviewCount + 1 " +
            "WHERE d.id = :designerId")
    void incrementReviewCount(Long designerId);

    @Modifying
    @Query("UPDATE Designer d SET d.reviewCount = d.reviewCount - 1 " +
            "WHERE d.id = :designerId AND d.reviewCount > 0")
    void decrementReviewCount(Long designerId);

    @Modifying
    @Query("UPDATE Designer d SET d.reservationCount = d.reservationCount + 1 " +
            "WHERE d.id = :designerId")
    void incrementReservationCount(Long designerId);

    @Modifying
    @Query("UPDATE Designer d SET d.reservationCount = d.reservationCount - 1 " +
            "WHERE d.id = :designerId AND d.reservationCount > 0")
    void decrementReservationCount(Long designerId);
}
