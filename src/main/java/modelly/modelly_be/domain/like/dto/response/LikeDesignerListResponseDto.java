package modelly.modelly_be.domain.like.dto.response;

import modelly.modelly_be.global.entity.Category;

public record LikeDesignerListResponseDto(
        Long designerLikeId,
        Long designerId,
        String designerName,
        String designerProfileImage,
        Category designerCategory,
        String shopName,
        String shopAddress,
        long reviewCount,
        double averageRating
) {
}
