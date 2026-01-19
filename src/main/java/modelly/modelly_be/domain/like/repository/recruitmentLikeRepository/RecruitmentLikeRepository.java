package modelly.modelly_be.domain.like.repository.recruitmentLikeRepository;

import modelly.modelly_be.domain.like.entity.RecruitmentLike;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.global.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecruitmentLikeRepository extends JpaRepository<RecruitmentLike, Long>, RecruitmentLikeRepositoryCustom {
    void deleteAllByRecruitment(Recruitment recruitment);

    boolean existsByModelAndRecruitment(Model model, Recruitment recruitment);

    void deleteByModelAndRecruitment(Model model, Recruitment recruitment);

    @Query("SELECT COUNT(rl.id) FROM RecruitmentLike rl " +
            "WHERE rl.model = :model AND (:category IS NULL OR rl.recruitment.category = :category) ")
    Long countByModelAndCategory(Model model, Category category);
}
