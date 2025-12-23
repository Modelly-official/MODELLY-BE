package modelly.modelly_be.domain.map.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.map.dto.response.ShopResponse;
import modelly.modelly_be.domain.map.service.MapService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.Coordinate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MapController implements MapSwagger{

    private final MapService mapService;

    //샵 리스트 조회
    public ApiResponse<List<ShopResponse>> getShopList(@AuthenticationPrincipal AuthDetails authDetails, int size, Category category, Double userLatitude, Double userLongitude) {
        Long userId = (authDetails != null ? authDetails.user().getId() : null);
        Coordinate coordinate = new Coordinate(userLatitude, userLongitude);
        List<ShopResponse> designerList = mapService.getShopList(userId, category, size, coordinate);

        return ApiResponse.onSuccess(designerList);
    }
}
