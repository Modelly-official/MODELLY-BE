package modelly.modelly_be.domain.like.repository.recruitmentLikeRepository;

import modelly.modelly_be.domain.like.dto.response.LikeRecruitmentListResponseDto;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

public interface RecruitmentLikeRepositoryCustom {

    List<LikeRecruitmentListResponseDto> findAllByConditions(Long id, Category category, Long cursorId, int size);
}
