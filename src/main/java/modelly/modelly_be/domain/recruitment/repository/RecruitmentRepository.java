package modelly.modelly_be.domain.recruitment.repository;

import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
}
