package modelly.modelly_be.domain.user.repository.designerRepository;

import modelly.modelly_be.domain.map.dto.response.ShopResponse;
import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.utils.Coordinate;
import modelly.modelly_be.global.utils.SearchCondition;

import java.util.List;

public interface DesignerRepositoryCustom {
    List<DesignerListResponseDto> findDesignersByCreatedAt(Long userId, SearchCondition searchCondition, Long cursorId, int size);

    List<DesignerListResponseDto> findDesignersByReviews(Long userId, SearchCondition searchCondition, Long cursorId, Long cursorReviewCount, int size);

    List<DesignerListResponseDto> findDesignersByDistance(Long userId, SearchCondition searchCondition, Long cursorId, Double cursorDistance, int size, Coordinate userCoordinate);

    List<ShopResponse> findShopsByDistance(Long userId, Category category, int size, Coordinate userCoordinate);
}
