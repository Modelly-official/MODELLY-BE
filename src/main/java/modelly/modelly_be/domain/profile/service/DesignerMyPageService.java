package modelly.modelly_be.domain.profile.service;

import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerMyPageRequest;
import modelly.modelly_be.domain.profile.dto.response.DesignerMyPageResponse;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.geocoding.GeoCodingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DesignerMyPageService {

    private final DesignerService designerService;
    private final GeoCodingService geoCodingService;

    @Transactional(readOnly = true)
    public DesignerMyPageResponse getMyPage(User user) {

        Designer designer = designerService.getByUser(user);
        return DesignerMyPageResponse.from(designer);
    }


    @Transactional
    public DesignerMyPageResponse updateMyPage(User user, UpdateDesignerMyPageRequest req) {

        Designer designer = designerService.getByUser(user);
        User me = designer.getUser();

        // 기본 마이페이지 정보 업데이트
        designer.updateMyPage(
                req.nickname(),
                req.intro(),
                req.shop(),
                req.category()
        );

        me.updateMyPage(
                req.gender(),
                req.birth(),
                req.profileImageUrl()
        );

        boolean addressChanged = !Objects.equals(designer.getAddressLine1(), req.addressLine1())
                || !Objects.equals(designer.getAddressLine2(), req.addressLine2());


        // 주소가 바뀐 경우만 위경도 갱신
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

        return DesignerMyPageResponse.from(designer);
    }
}
