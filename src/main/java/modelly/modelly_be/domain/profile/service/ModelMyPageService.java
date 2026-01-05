package modelly.modelly_be.domain.profile.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerMyPageRequest;
import modelly.modelly_be.domain.profile.dto.request.UpdateModelMyPageRequest;
import modelly.modelly_be.domain.profile.dto.response.DesignerMyPageResponse;
import modelly.modelly_be.domain.profile.dto.response.ModelMyPageResponse;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.ModelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModelMyPageService {

    private final ModelService modelService;

    @Transactional(readOnly = true)
    public ModelMyPageResponse getMyPage(User user) {

        Model model = modelService.getModelByUser(user);
        return ModelMyPageResponse.from(model);
    }

    @Transactional
    public ModelMyPageResponse updateMyPage(User user, UpdateModelMyPageRequest req) {

        Model model = modelService.getModelByUser(user);

        model.updateNickname(
                req.nickname()
        );

        user.updateMyPage(
                req.gender(),
                req.birth(),
                req.profileImageUrl()
        );

        return ModelMyPageResponse.from(model);
    }
}
