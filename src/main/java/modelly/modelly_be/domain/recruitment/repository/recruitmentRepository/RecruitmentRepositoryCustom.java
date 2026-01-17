package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse;
import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentBasic;
import modelly.modelly_be.domain.recruitment.dto.internal.DesignerRecruitmentList;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.Coordinate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RecruitmentRepositoryCustom {


    List<RecruitmentBasic> findRecruitmentsByCreatedAt(Long userId, SearchCondition searchCondition, Long cursorId, int size, Coordinate userCoordinate);

    List<RecruitmentBasic> findRecruitmentsByReviews(Long userId, SearchCondition searchCondition, Long cursorId, Long cursorReviewCount, int size, Coordinate userCoordinate);

    List<RecruitmentBasic> findRecruitmentsByDistance(Long userId, SearchCondition searchCondition, Long cursorId, Double cursorDistance, int size, Coordinate userCoordinate);

    List<DesignerRecruitmentList> findRecruitmentsByDesignerAndDate(Designer designer, YearMonth yearMonth, int size, LocalDate cursorEarliestDate, Long cursorId, RecruitmentStatus status);

    Map<Long, Set<SubCategory>> findSubCategoriesByRecruitmentIds(List<Long> recruitmentIds);

    List<DesignerProfileResponse.RecruitmentCard> findOpenRecruitmentsByDesigner(Long designerId);
}
