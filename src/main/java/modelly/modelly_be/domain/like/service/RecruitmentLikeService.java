package modelly.modelly_be.domain.like.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.entity.RecruitmentLike;
import modelly.modelly_be.domain.like.repository.RecruitmentLikeRepository;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;

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
}
