package modelly.modelly_be.domain.user.dto;

import modelly.modelly_be.domain.user.entity.Designer;

public record DesignerResponseDto(
        Long userId,
        Long designerId,
        String shop,
        String shopAddress
) {
    public static DesignerResponseDto from(Designer designer) {
        return new DesignerResponseDto(
                designer.getUser().getId(),
                designer.getId(),
                designer.getShop(),
                designer.getShopAddress()
        );
    }
}
