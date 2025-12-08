package modelly.modelly_be.domain.like.dto.response;

public record LikeDesignerListResponseDto(
        Long designerLikeId,
        Long designerId,
        String designerName,
        String designerProfileImage,
        String designerCategory,
        String shopName,
        String shopAddress
) {
}
