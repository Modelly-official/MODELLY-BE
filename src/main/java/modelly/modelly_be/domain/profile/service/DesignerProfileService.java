package modelly.modelly_be.domain.profile.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerProfileRequest;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse.DesignerProfileInfo;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse.Address;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse.RecruitmentCard;
import modelly.modelly_be.domain.profile.repository.ProfileRecruitmentQueryRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignerProfileService {

    private final DesignerService designerService;
    private final ProfileRecruitmentQueryRepository profileRecruitmentQueryRepository;

    // 모델/게스트용 디자니어 프로필 조회
    @Transactional(readOnly = true)
    public DesignerProfileResponse getPublicDesignerProfile(Long designerId) {
        Designer designer = designerService.getById(designerId);
        return buildResponse(designer);
    }

    // 디자이너 마이프로필 조회용
    @Transactional(readOnly = true)
    public DesignerProfileResponse getMyDesignerProfile(User user) {
        if (user.getUserRole() != UserRole.DESIGNER) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
        Designer designer = designerService.getByUser(user);
        return buildResponse(designer);
    }

    // 프로필 조회 반환 정보 생성
    private DesignerProfileResponse buildResponse(Designer designer) {
        DesignerProfileInfo info = new DesignerProfileInfo(
                designer.getUser().getId(),
                designer.getId(),
                designer.getNickname(),
                designer.getUser().getImageUrl(),
                designer.getShop(),
                new Address(designer.getAddressLine1(), designer.getAddressLine2()),
                designer.getIntro()
        );

        List<RecruitmentCard> openRecruitments =
                profileRecruitmentQueryRepository.findOpenRecruitmentsByDesigner(designer.getId());

        return DesignerProfileResponse.of(info, openRecruitments);
    }

    // 디자이너의 내 프로필 수정
    @Transactional
    public DesignerProfileResponse updateMyDesignerProfile(User user, UpdateDesignerProfileRequest req) {
        Designer designer = designerService.getByUser(user);
        User me = designer.getUser();

        if (req.nickname() != null) {designer.updateNickname(req.nickname());}
        if (req.intro() != null) designer.updateIntro(req.intro());
        if (req.shop() != null) designer.updateShop(req.shop());
        if (req.addressLine1() != null) designer.updateAddressLine1(req.addressLine1());
        if (req.addressLine2() != null) designer.updateAddressLine2(req.addressLine2());

        if (req.profileImageUrl() != null) {me.updateImageUrl(req.profileImageUrl());}

        // 응답은 최신 정보로 다시 조립
        DesignerProfileInfo info = new DesignerProfileInfo(
                designer.getUser().getId(),
                designer.getId(),
                designer.getNickname(),
                safeImageUrl(designer),
                designer.getShop(),
                new Address(designer.getAddressLine1(), designer.getAddressLine2()),
                designer.getIntro()
        );

        return DesignerProfileResponse.of(
                info,
                profileRecruitmentQueryRepository.findOpenRecruitmentsByDesigner(designer.getId())
        );
    }

    // image null 가능으로 safe check
    private String safeImageUrl(Designer designer) {
        try {
            return designer.getUser().getImageUrl();
        } catch (Exception e) {
            return null;
        }
    }
}
