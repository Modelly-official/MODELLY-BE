package modelly.modelly_be.domain.recruitment.dto.common;
import modelly.modelly_be.global.entity.Category;

import java.time.LocalDateTime;

public record RecruitmentBasic(
        Long recruitmentId,
        String title,
        String designerImage,
        String designerName,
        String recruitmentThumbnail,
        String shop,
        String shopAddress,
        Category category,
        Long reviewCount,
        Double distance,
        boolean isLiked,
        LocalDateTime createdAt
) {
}
