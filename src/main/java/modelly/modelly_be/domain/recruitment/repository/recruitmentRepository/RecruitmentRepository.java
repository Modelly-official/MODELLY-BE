package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;


public interface RecruitmentRepository extends JpaRepository<Recruitment, Long>,RecruitmentRepositoryCustom {

    @Query("""
        SELECT DISTINCT r
        FROM Recruitment r
        WHERE r.id = :recruitmentId
    """)
    @EntityGraph(attributePaths = {"designer"})
    Recruitment findByIdWithAllDetails(Long recruitmentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Recruitment r " +
            "SET r.recruitmentStatus = 'CLOSED' " +
            "WHERE r.deadline < :today AND r.recruitmentStatus = 'OPEN'")
    int updateStatusToClosed(LocalDate today);

}
