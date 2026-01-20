package modelly.modelly_be.domain.like.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import modelly.modelly_be.global.entity.Category;

public record LikeDesignerListResponseDto(
        Long designerLikeId,
        Long designerId,
        String designerName,
        String designerProfileImage,
        @JsonSerialize(using = Category.CategorySerializer.class)
        Category designerCategory,
        String shopName,
        String shopAddress,
        long reviewCount,
        double averageRating
) {
}
