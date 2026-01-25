package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface RecruitmentRepository extends JpaRepository<Recruitment, Long>,RecruitmentRepositoryCustom {

    @Query("""
        SELECT DISTINCT r
        FROM Recruitment r
        WHERE r.id = :recruitmentId
    """)
    @EntityGraph(attributePaths = {"designer"})
    Optional<Recruitment> findByIdWithAllDetails(Long recruitmentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Recruitment r " +
            "SET r.recruitmentStatus = 'CLOSED' " +
            "WHERE r.deadline < :today AND r.recruitmentStatus = 'OPEN'")
    int updateStatusToClosed(LocalDate today);


    /*---------- ----------*/
    @Query("""
    select distinct r
    from Recruitment r
    left join fetch r.recruitmentDates rd
    left join fetch rd.recruitmentTimes rt
    where r.id = :recruitmentId
      and rd.date >= :startDate
      and rd.date < :endDate
    """)
    Optional<Recruitment> findByIdWithScheduleInRange(
            @Param("recruitmentId") Long recruitmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(distinct r.id) FROM Recruitment r " +
            "JOIN r.recruitmentDates rd " +
            "WHERE r.designer = :designer " +
            "AND r.recruitmentStatus = :status " +
            "AND YEAR(rd.date) = :year AND MONTH(rd.date) = :month ")
    Long countByDesignerAndDateAndStatus(Designer designer, int year, int month, RecruitmentStatus status);

    @Query("SELECT COUNT(DISTINCT r.id) FROM Recruitment r " +
            "LEFT JOIN r.subCategoryList sc " +
            "WHERE r.recruitmentStatus = :status "+
            "AND (:keyword IS NULL OR r.title LIKE %:keyword%) " +
            "AND (:category IS NULL OR r.category = :category) " +
            "AND (:subCategory IS NULL OR sc = :subCategory) ")
    Long countByCondition(String keyword, Category category, SubCategory subCategory, RecruitmentStatus status);

    // 해당 디자이너의 공고 조회
    List<Recruitment> findAllByDesignerId(Long designerId);


    // 해당 디자이너의 공고 삭제
    @Modifying
    @Query("delete from Recruitment r where r.designer.id = :designerId")
    void deleteByDesignerId(Long designerId);
}
