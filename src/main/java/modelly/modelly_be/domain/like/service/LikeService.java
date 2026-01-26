package modelly.modelly_be.domain.like.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.dto.response.LikeDesignerListResponseDto;
import modelly.modelly_be.domain.like.dto.response.LikeRecruitmentListResponseDto;
import modelly.modelly_be.domain.like.entity.DesignerLike;
import modelly.modelly_be.domain.like.entity.RecruitmentLike;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final RecruitmentService recruitmentService;
    private final ModelService modelService;
    private final RecruitmentLikeService recruitmentLikeService;
    private final DesignerService designerService;
    private final DesignerLikeService designerLikeService;

    @Transactional
    public void recruitmentLikeOrLikeCancel(User user, Long recruitmentId) {
        Recruitment recruitment = recruitmentService.getById(recruitmentId);

        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        boolean alreadyExists = recruitmentLikeService.existsByModelAndRecruitment(model,recruitment);

        if (alreadyExists) {
            recruitmentLikeService.deleteByModelAndRecruitment(model, recruitment);
            recruitment.decrementLikeCount();
        } else {
            RecruitmentLike recruitmentLike = RecruitmentLike.builder()
                    .recruitment(recruitment)
                    .model(model)
                    .build();

            recruitment.incrementLikeCount();

            recruitmentLikeService.save(recruitmentLike);
        }
    }

    @Transactional(readOnly = true)
    public ScrollResponse<LikeRecruitmentListResponseDto> getLikeRecruitmentList(User user, Category category, Long cursorId, int size) {
        modelService.checkModel(user);
        List<LikeRecruitmentListResponseDto> likeList = recruitmentLikeService.getLikeRecruitmentList(user,category,cursorId,size);

        Model model = modelService.getModelByUser(user);

        Long totalCount = recruitmentLikeService.countByModelAndCategory(model, category);

        ScrollResponse<LikeRecruitmentListResponseDto> responseDtos = ScrollUtil.paginate(likeList,size, totalCount);

        return responseDtos;
    }

    @Transactional
    public void designerLikeOrLikeCancel(User user, Long designerId) {
        Designer designer = designerService.getById(designerId);

        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        boolean alreadyExists = designerLikeService.existsByModelAndDesigner(model, designer);

        if (alreadyExists) {
            designerLikeService.deleteByModelAndDesigner(model, designer);
            designer.decrementLikeCount();
        } else {
            DesignerLike designerLike = DesignerLike.builder()
                    .designer(designer)
                    .model(model)
                    .build();

            designer.incrementLikeCount();

            designerLikeService.save(designerLike);
        }
    }

    @Transactional(readOnly = true)
    public ScrollResponse<LikeDesignerListResponseDto> getLikeDesignerList(User user, Category category, Long cursorId, int size) {
        modelService.checkModel(user);

        List<LikeDesignerListResponseDto> likeList = designerLikeService.getLikeDesignerList(user, category, cursorId, size);

        Model model = modelService.getModelByUser(user);

        Long totalCount = designerLikeService.countByModelAndCategory(model, category);

        ScrollResponse<LikeDesignerListResponseDto> responseDtos = ScrollUtil.paginate(likeList,size, totalCount);

        return responseDtos;
    }
}
