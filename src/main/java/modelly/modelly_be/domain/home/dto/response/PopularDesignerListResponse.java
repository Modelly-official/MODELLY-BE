package modelly.modelly_be.domain.home.dto.response;

import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;

import java.util.List;

public record PopularDesignerListResponse(
        List<DesignerListResponseDto> designerList,
        int totalPages,
        boolean hasNext
) {
}
