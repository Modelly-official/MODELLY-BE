package modelly.modelly_be.domain.like.repository;

import modelly.modelly_be.domain.like.entity.RecruitmentLike;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentLikeRepository extends JpaRepository<RecruitmentLike, Long> {
    void deleteAllByRecruitment(Recruitment recruitment);
}
