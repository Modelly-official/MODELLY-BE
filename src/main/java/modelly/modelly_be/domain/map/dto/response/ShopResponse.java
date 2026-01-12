package modelly.modelly_be.domain.map.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import modelly.modelly_be.global.entity.Category;

public record ShopResponse(
    Long designerId,
    String shopName,
    @JsonSerialize(using = Category.CategorySerializer.class)
    Category category,
    Double shopLatitude,
    Double shopLongitude
) {
}
