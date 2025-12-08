package modelly.modelly_be.domain.like.dto.response;

public record LikeRecruitmentListResponseDto(
        Long recruitmentLikeId,
        Long recruitmentId,
        String thumbnail
) {
}
