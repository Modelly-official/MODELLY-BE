package modelly.modelly_be.domain.recruitment.dto.internal;


public record DesignerRecruitmentList(
        Long recruitmentId,
        String title,
        String period,
        String thumbnail,
        Long reviewCount,
        double averageRating
) {
}
