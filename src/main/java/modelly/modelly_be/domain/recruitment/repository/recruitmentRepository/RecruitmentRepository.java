package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentBasic;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.global.utils.Coordinate;
import modelly.modelly_be.global.utils.SearchCondition;
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

}
