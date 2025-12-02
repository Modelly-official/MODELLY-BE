package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.Coordinate;

import java.util.List;

public interface RecruitmentRepositoryCustom {


    List<RecruitmentListResponseDto> findRecruitmentsByCreatedAt(Long userId, SearchCondition searchCondition, Long cursorId, int size);

    List<RecruitmentListResponseDto> findRecruitmentsByReviews(Long userId, SearchCondition searchCondition, Long cursorId, Long cursorReviewCount, int size);

    List<RecruitmentListResponseDto> findRecruitmentsByDistance(Long userId, SearchCondition searchCondition, Long cursorId, Double cursorDistance, int size, Coordinate userCoordinate);
}
