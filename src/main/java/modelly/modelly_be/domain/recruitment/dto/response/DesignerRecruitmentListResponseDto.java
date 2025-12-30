package modelly.modelly_be.domain.recruitment.dto.response;



public record DesignerRecruitmentListResponseDto(
        Long recruitmentId,
        String title,
        String period,
        String thumbnail,
        Long reviewCount,
        double averageRating
) {
}
