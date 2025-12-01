package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.global.entity.SortOption;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.UserCoordinate;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface RecruitmentRepositoryCustom {

    Slice<RecruitmentListResponseDto> findRecruitmentsByConditions(Long userId,
                                                                   SearchCondition searchCondition,
                                                                   SortOption sortOption,
                                                                   Long cursorId,
                                                                   int size,
                                                                   UserCoordinate userCoordinate);
}
