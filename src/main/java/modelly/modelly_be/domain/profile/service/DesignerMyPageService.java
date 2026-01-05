package modelly.modelly_be.domain.profile.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerMyPageRequest;
import modelly.modelly_be.domain.profile.dto.response.DesignerMyPageResponse;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignerMyPageService {

    private final DesignerService designerService;

    @Transactional(readOnly = true)
    public DesignerMyPageResponse getMyPage(User user) {

        Designer designer = designerService.getByUser(user);
        return DesignerMyPageResponse.from(designer);
    }


    @Transactional
    public DesignerMyPageResponse updateMyPage(User user, UpdateDesignerMyPageRequest req) {

        Designer designer = designerService.getByUser(user);
        User me = designer.getUser();

        designer.updateMyPage(
                req.nickname(),
                req.intro(),
                req.shop(),
                req.addressLine1(),
                req.addressLine2(),
                req.category()
        );

        me.updateMyPage(
                req.gender(),
                req.birth(),
                req.profileImageUrl()
        );

        return DesignerMyPageResponse.from(designer);
    }
}
