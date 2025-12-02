package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface RecruitmentRepository extends JpaRepository<Recruitment, Long>,RecruitmentRepositoryCustom {

    @Query("""
        SELECT DISTINCT r
        FROM Recruitment r
        WHERE r.id = :recruitmentId
    """)
    @EntityGraph(attributePaths = {"designer"})
    Recruitment findByIdWithAllDetails(Long recruitmentId);
}
