package modelly.modelly_be.domain.recruitment.dto.response;

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
        List<String> subCategories,
        Long reviewCount,
        Double distance,
        boolean isLiked,
        LocalDateTime createdAt,
        Double averageRating
) {
}
