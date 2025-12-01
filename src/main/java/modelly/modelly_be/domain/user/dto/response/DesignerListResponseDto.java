package modelly.modelly_be.domain.user.dto.response;

import modelly.modelly_be.domain.recruitment.entity.SubCategory;
import modelly.modelly_be.global.entity.Category;

import java.time.LocalDateTime;

public record DesignerListResponseDto(
        Long designerId,
        String designerName,
        String shop,
        String shopAddress,
        String thumbnail,
        Category category,
        Long reviewCount,
        Double distance,
        boolean isLiked,
        LocalDateTime createdAt
) {
}
