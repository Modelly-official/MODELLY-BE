package modelly.modelly_be.domain.profile.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse.RecruitmentCard;
import modelly.modelly_be.domain.recruitment.entity.QRecruitmentDate;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.domain.recruitment.entity.QRecruitment;
import modelly.modelly_be.global.entity.SubCategory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProfileRecruitmentQueryRepositoryImpl implements ProfileRecruitmentQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RecruitmentCard> findOpenRecruitmentsByDesigner(Long designerId) {
        QRecruitment r = QRecruitment.recruitment;
        QRecruitmentDate rd = QRecruitmentDate.recruitmentDate;

        DateExpression<LocalDate> startDate = rd.date.min();

        // 공고 정보 tuple로
        List<Tuple> rows = queryFactory
                .select(
                        r.id,
                        r.title,
                        r.thumbnail,
                        startDate,
                        r.deadline
                )
                .from(r)
                .join(r.recruitmentDates, rd)
                .where(
                        r.designer.id.eq(designerId),
                        r.recruitmentStatus.eq(RecruitmentStatus.OPEN)
                )
                .groupBy(r.id, r.title, r.thumbnail, r.deadline)
                .orderBy(r.deadline.asc().nullsLast(), r.id.asc())
                .fetch();

        if (rows.isEmpty()) return List.of();

        // 공고 id 목록
        List<Long> recruitmentIds = rows.stream()
                .map(t -> t.get(r.id))
                .toList();

        // subCategory를 한 번에 조회해서 recruitmentId -> [description] 매핑
        Map<Long, List<String>> subCategoryMap = fetchSubCategoriesByRecruitmentIds(recruitmentIds);

        // DTO 반환
        return rows.stream()
                .map(t -> {
                    Long recruitmentId = t.get(r.id);
                    return new RecruitmentCard(
                            recruitmentId,
                            t.get(r.title),
                            t.get(r.thumbnail),
                            t.get(startDate),
                            t.get(r.deadline),
                            subCategoryMap.getOrDefault(recruitmentId, List.of())
                    );
                })
                .toList();
    }

    // 서브 카테고리 조회
    private Map<Long, List<String>> fetchSubCategoriesByRecruitmentIds(List<Long> recruitmentIds) {
        if (recruitmentIds == null || recruitmentIds.isEmpty()) return Map.of();

        QRecruitment r = QRecruitment.recruitment;

        EnumPath<SubCategory> sub = Expressions.enumPath(SubCategory.class, "sub");

        List<Tuple> subRows = queryFactory
                .select(r.id, sub)
                .from(r)
                .join(r.subCategoryList, sub)
                .where(r.id.in(recruitmentIds))
                .fetch();

        return subRows.stream()
                .collect(Collectors.groupingBy(
                        t -> t.get(r.id),
                        Collectors.mapping(
                                t -> ((SubCategory) t.get(sub)).getDescription(),
                                Collectors.toList()
                        )
                ));
    }
}
