package modelly.modelly_be.domain.like.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.dto.LikeRecruitmentListResponseDto;
import modelly.modelly_be.domain.like.entity.RecruitmentLike;
import modelly.modelly_be.domain.like.repository.recruitmentLikeRepository.RecruitmentLikeRepository;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentLikeService {

    private final RecruitmentLikeRepository recruitmentLikeRepository;

    public void deleteRecruitmentLike(Recruitment recruitment) {
        recruitmentLikeRepository.deleteAllByRecruitment(recruitment);
    }

    public boolean existsByModelAndRecruitment(Model model, Recruitment recruitment) {
        return recruitmentLikeRepository.existsByModelAndRecruitment(model, recruitment);
    }

    public void deleteByModelAndRecruitment(Model model, Recruitment recruitment) {
        recruitmentLikeRepository.deleteByModelAndRecruitment(model, recruitment);
    }

    public void save(RecruitmentLike recruitmentLike) {
        recruitmentLikeRepository.save(recruitmentLike);
    }

    public List<LikeRecruitmentListResponseDto> getLikeRecruitmentList(User user, Category category, Long cursorId, int size) {
        return recruitmentLikeRepository.findAllByConditions(user.getId(), category, cursorId, size);
    }
}
