package modelly.modelly_be.domain.recruitment.dto.response;
import modelly.modelly_be.global.entity.SubCategory;

import java.time.LocalDateTime;
import java.util.List;

public record RecruitmentListResponseDto(
        Long recruitmentId,
        String title,
        String designerImage,
        String designerName,
        String recruitmentThumbnail,
        String shop,
        String shopAddress,
        String category,
        List<SubCategory> subCategories,
        Long reviewCount,
        Double distance,
        boolean isLiked,
        LocalDateTime createdAt,
        Double averageRating
) {
}
