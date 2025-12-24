package modelly.modelly_be.domain.map.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.map.dto.response.ShopResponse;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.utils.Coordinate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final DesignerService designerService;

    @Transactional(readOnly = true)
    public List<ShopResponse> getShopList(Long userId, Category category, int size, Coordinate coordinate) {

        List<ShopResponse> shopList = designerService.getShopList(userId, category, size, coordinate);

        return shopList;
    }
}
