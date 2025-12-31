package modelly.modelly_be.domain.recruitment.dto.response;

import java.util.List;

public record DesignerRecruitmentListResponse(
        Long recruitmentId,
        String title,
        String period,
        String thumbnail,
        Long reviewCount,
        double averageRating,
        List<String> subCategory
) {
}
