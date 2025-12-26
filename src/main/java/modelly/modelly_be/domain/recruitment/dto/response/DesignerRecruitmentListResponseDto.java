package modelly.modelly_be.domain.recruitment.dto.response;


import java.time.LocalDate;

public record DesignerRecruitmentListResponseDto(
        Long recruitmentId,
        String title,
        String period,
        Long reviewCount,
        double averageRating
) {
}
