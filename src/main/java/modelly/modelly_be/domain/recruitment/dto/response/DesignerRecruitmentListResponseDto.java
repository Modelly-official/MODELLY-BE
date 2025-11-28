package modelly.modelly_be.domain.recruitment.dto.response;


public record DesignerRecruitmentListResponseDto(
        Long recruitmentId,
        String createdAt,
        String title,
        String category
) {
}
