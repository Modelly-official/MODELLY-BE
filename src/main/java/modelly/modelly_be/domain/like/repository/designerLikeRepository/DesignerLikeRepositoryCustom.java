package modelly.modelly_be.domain.like.repository.designerLikeRepository;

import modelly.modelly_be.domain.like.dto.response.LikeDesignerListResponseDto;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

public interface DesignerLikeRepositoryCustom {
    List<LikeDesignerListResponseDto> findAllByConditions(Long userId, Category category, Long cursorId, int size);
}
