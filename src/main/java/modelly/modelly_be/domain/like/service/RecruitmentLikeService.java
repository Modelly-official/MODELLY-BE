package modelly.modelly_be.domain.like.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.repository.RecruitmentLikeRepository;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentLikeService {

    private final RecruitmentLikeRepository recruitmentLikeRepository;

    public void deleteRecruitmentLike(Recruitment recruitment) {
        recruitmentLikeRepository.deleteAllByRecruitment(recruitment);
    }
}
