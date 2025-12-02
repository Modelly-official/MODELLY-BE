package modelly.modelly_be.domain.recruitment.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.dto.LikeRecruitmentListResponseDto;
import modelly.modelly_be.domain.like.entity.RecruitmentLike;
import modelly.modelly_be.domain.like.service.RecruitmentLikeService;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.entity.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final RecruitmentService recruitmentService;
    private final ModelService modelService;
    private final RecruitmentLikeService recruitmentLikeService;

    @Transactional
    public void recruitmentLikeOrLikeCancel(User user, Long recruitmentId) {
        Recruitment recruitment = recruitmentService.getById(recruitmentId);

        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        boolean alreadyExists = recruitmentLikeService.existsByModelAndRecruitment(model,recruitment);

        if (alreadyExists) {
            recruitmentLikeService.deleteByModelAndRecruitment(model, recruitment);
        } else {
            RecruitmentLike recruitmentLike = RecruitmentLike.builder()
                    .recruitment(recruitment)
                    .model(model)
                    .build();

            recruitmentLikeService.save(recruitmentLike);
        }
    }

    public List<LikeRecruitmentListResponseDto> getLikeRecruitmentList(User user, Category category, Long cursorId, int size) {
        List<LikeRecruitmentListResponseDto> likeRecruitmentListResponseDtos = recruitmentLikeService.getLikeRecruitmentList(user,category,cursorId,size);
        return likeRecruitmentListResponseDtos;
    }
}
