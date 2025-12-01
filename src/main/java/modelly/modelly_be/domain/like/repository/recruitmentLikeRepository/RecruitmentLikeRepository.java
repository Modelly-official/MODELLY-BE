package modelly.modelly_be.domain.like.repository.recruitmentLikeRepository;

import modelly.modelly_be.domain.like.entity.RecruitmentLike;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentLikeRepository extends JpaRepository<RecruitmentLike, Long>, RecruitmentLikeRepositoryCustom {
    void deleteAllByRecruitment(Recruitment recruitment);

    boolean existsByModelAndRecruitment(Model model, Recruitment recruitment);

    void deleteByModelAndRecruitment(Model model, Recruitment recruitment);


}
