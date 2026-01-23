package modelly.modelly_be.domain.user.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import modelly.modelly_be.global.entity.Category;

import java.time.LocalDateTime;

public record DesignerListResponseDto(
        Long designerId,
        String designerName,
        String shop,
        String shopAddress,
        String designerProfileImage,
        @JsonSerialize(using = Category.CategorySerializer.class)
        Category category,
        Long reviewCount,
        Double distance,
        boolean isLiked,
        LocalDateTime createdAt,
        double averageRating
) {
}
