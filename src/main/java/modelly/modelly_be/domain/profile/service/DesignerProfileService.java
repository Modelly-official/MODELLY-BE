package modelly.modelly_be.domain.profile.service;

import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.service.DesignerLikeService;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerProfileRequest;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse.DesignerProfileInfo;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse.Address;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse.RecruitmentCard;
import modelly.modelly_be.domain.recruitment.repository.recruitmentRepository.RecruitmentRepository;
import modelly.modelly_be.domain.review.dto.internal.AverageReview;
import modelly.modelly_be.domain.review.service.ReviewService;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.domain.user.service.UserService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.geocoding.GeoCodingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DesignerProfileService {

    private final DesignerService designerService;
    private final ModelService modelService;
    private final ReviewService reviewService;
    private final DesignerLikeService designerLikeService;
    private final RecruitmentRepository recruitmentRepository;
    private final UserService userService;
    private final GeoCodingService geoCodingService;

    // 모델/게스트용 디자니어 프로필 조회
    @Transactional(readOnly = true)
    public DesignerProfileResponse getPublicDesignerProfile(User user, Long designerId) {
        Designer designer = designerService.getById(designerId);
        return buildResponse(user, designer);
    }

    // 디자이너 마이프로필 조회용
    @Transactional(readOnly = true)
    public DesignerProfileResponse getMyDesignerProfile(User user) {
        if (user.getUserRole() != UserRole.DESIGNER) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
        Designer designer = designerService.getByUser(user);
        return buildResponse(user, designer);
    }

    // 프로필 조회 반환 정보 생성
    private DesignerProfileResponse buildResponse(User user, Designer designer) {
        boolean isLiked = false;

        // user가 모델일 경우 like 여부 조회
        if (user != null && user.getUserRole() == UserRole.MODEL) {
            Model model = modelService.getModelByUser(user);
            isLiked = designerLikeService.existsByModelAndDesigner(model, designer);
        }

        AverageReview reviewInfo = reviewService.calculateRating(designer);

        DesignerProfileInfo info = new DesignerProfileInfo(
                designer.getUser().getId(),
                designer.getId(),
                designer.getNickname(),
                designer.getUser().getImageUrl(),
                designer.getShop(),
                new Address(designer.getAddressLine1(), designer.getAddressLine2()),
                designer.getIntro(),
                isLiked,
                reviewInfo.totalCount(),
                reviewInfo.averageRating()
        );

        List<RecruitmentCard> openRecruitments =
                recruitmentRepository.findOpenRecruitmentsByDesigner(designer.getId());

        return DesignerProfileResponse.of(info, openRecruitments);
    }

    // 디자이너의 내 프로필 수정
    @Transactional
    public DesignerProfileResponse updateMyDesignerProfile(User user, UpdateDesignerProfileRequest req) {
        Designer designer = designerService.getByUser(user);
        User managedUser = userService.getById(user.getId());

        // 기본 정보 업데이트
        designer.updateProfile(
                req.nickname(),
                req.intro(),
                req.shop()
        );

        boolean addressChanged = !Objects.equals(designer.getAddressLine1(), req.addressLine1())
                || !Objects.equals(designer.getAddressLine2(), req.addressLine2());

        // 주소가 바뀐 경우만 주소 update
        if (addressChanged) {
            LatLng latLng = geoCodingService.getLatLngRes(
                    req.addressLine1() + " " + req.addressLine2()
            );

            if (latLng == null) {
                throw new GeneralException(ErrorStatus.GEOCODING_FAILED);
            }

            designer.updateLocation(
                    req.addressLine1(),
                    req.addressLine2(),
                    latLng.lat,
                    latLng.lng
            );
        }

        // 이미지 업데이트
        if (req.profileImageUrl() != null) {
            managedUser.updateImageUrl(req.profileImageUrl());
        }

        AverageReview reviewInfo = reviewService.calculateRating(designer);

        // 응답은 최신 정보로 다시 조립
        DesignerProfileInfo info = new DesignerProfileInfo(
                user.getId(),
                designer.getId(),
                designer.getNickname(),
                safeImageUrl(designer),
                designer.getShop(),
                new Address(designer.getAddressLine1(), designer.getAddressLine2()),
                designer.getIntro(),
                false,
                reviewInfo.totalCount(),
                reviewInfo.averageRating()
        );

        return DesignerProfileResponse.of(
                info,
                recruitmentRepository.findOpenRecruitmentsByDesigner(designer.getId())
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
