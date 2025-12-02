package modelly.modelly_be.domain.recruitment.dto.response;

import modelly.modelly_be.domain.recruitment.entity.SubCategory;
import modelly.modelly_be.global.entity.Category;

import java.time.LocalDateTime;

public record RecruitmentListResponseDto(
        Long recruitmentId,
        String title,
        String designerImage,
        String designerName,
        String recruitmentThumbnail,
        String shop,
        String shopAddress,
        Category category,
        SubCategory subCategory,
        Long reviewCount,
        Double distance,
        boolean isLiked,
        LocalDateTime createdAt
) {
}
