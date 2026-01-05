package modelly.modelly_be.domain.profile.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.profile.dto.response.DesignerMyPageResponse;
import modelly.modelly_be.domain.profile.dto.response.DesignerMyPageResponse.Address;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignerMyPageService {

    private final DesignerService designerService;

    @Transactional(readOnly = true)
    public DesignerMyPageResponse getMyPage(User user) {

        Designer designer = designerService.getByUser(user);
        return new DesignerMyPageResponse(
                designer.getId(),
                designer.getNickname(),
                user.getGender().getDescription(),
                user.getBirth(),
                designer.getIntro(),
                designer.getShop(),
                new Address(designer.getAddressLine1(), designer.getAddressLine2()),
                designer.getCategory().getDescription(),
                user.getImageUrl()
        );
    }
}
