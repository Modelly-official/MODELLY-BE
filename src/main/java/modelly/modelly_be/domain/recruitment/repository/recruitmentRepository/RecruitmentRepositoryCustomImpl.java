package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.like.entity.QRecruitmentLike;
import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentBasic;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.entity.QRecruitment;
import modelly.modelly_be.domain.recruitment.entity.QRecruitmentDate;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.domain.review.entity.QReview;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.QDesigner;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.Coordinate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RecruitmentRepositoryCustomImpl implements RecruitmentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private static final QRecruitment qRecruitment = QRecruitment.recruitment;
    private static final QDesigner qDesigner = QDesigner.designer;
    private static final QRecruitmentLike qRecruitmentLike = QRecruitmentLike.recruitmentLike;
    private static final QReview qReview = QReview.review;
    private static final QRecruitmentDate qRecruitmentDate = QRecruitmentDate.recruitmentDate;

    @Override
    public List<RecruitmentBasic> findRecruitmentsByCreatedAt(Long userId, SearchCondition searchCondition, Long cursorId, int size, Coordinate userCoordinate) {

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);
        if (cursorId != null) {
            booleanBuilder.and(qRecruitment.id.lt(cursorId));
        }

        return fetchRecruitmentList(userId, booleanBuilder, userCoordinate, size, qRecruitment.id.desc());
    }

    @Override
    public List<RecruitmentBasic> findRecruitmentsByReviews(Long userId, SearchCondition searchCondition, Long cursorId, Long cursorReviewCount, int size, Coordinate userCoordinate) {

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        NumberExpression<Long> reviewCount = Expressions.asNumber(getReviewCountSubQuery());

        if (cursorId != null && cursorReviewCount != null) {
            booleanBuilder.and(
                    reviewCount.lt(cursorReviewCount)
                            .or(reviewCount.eq(cursorReviewCount)
                                    .and(qRecruitment.id.lt(cursorId)))
            );
        }

        return fetchRecruitmentList(userId, booleanBuilder, null, size, reviewCount.desc());
    }

    @Override
    public List<RecruitmentBasic> findRecruitmentsByDistance(Long userId, SearchCondition searchCondition, Long cursorId, Double cursorDistance, int size, Coordinate userCoordinate) {

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        NumberExpression<Double> distance = getDistanceExpression(userCoordinate);

        if (cursorId != null && cursorDistance != null) {
            booleanBuilder.and(
                    distance.gt(cursorDistance)
                            .or(distance.eq(cursorDistance)
                                    .and(qRecruitment.id.lt(cursorId)))
            );
        }

        return fetchRecruitmentList(userId, booleanBuilder, userCoordinate, size, distance.asc());
    }

    @Override
    public List<DesignerRecruitmentListResponseDto> findRecruitmentsByDesignerAndDate(Designer designer, YearMonth yearMonth, int size, LocalDate cursorEarliestDate, Long cursorId) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qRecruitment.designer.id.eq(designer.getId()));

        booleanBuilder.and(qRecruitmentDate.date.year().eq(yearMonth.getYear())
                .and(qRecruitmentDate.date.month().eq(yearMonth.getMonthValue())));

        NumberPath<Long> reviewCount = Expressions.numberPath(Long.class, "reviewCount");
        JPQLSubQuery<Long> reviewCountSubQuery=JPAExpressions
                .select(qReview.count())
                .from(qReview)
                .where(qReview.designer.eq(qRecruitment.designer));

        NumberExpression<Long> reviewCountExpression = Expressions.asNumber(ExpressionUtils.as(reviewCountSubQuery, reviewCount));

        QRecruitmentDate subDate = new QRecruitmentDate("subDate");

        DateExpression<LocalDate> minDate = Expressions.dateTemplate(LocalDate.class,
                "(SELECT MIN({0}.date) FROM RecruitmentDate {0} WHERE {0}.recruitment = {1})", subDate, qRecruitment);
        DateExpression<LocalDate> maxDate = Expressions.dateTemplate(LocalDate.class,
                "(SELECT MAX({0}.date) FROM RecruitmentDate {0} WHERE {0}.recruitment = {1})", subDate, qRecruitment);

        StringExpression dateRangeExpression = Expressions.stringTemplate(
                "CONCAT(CAST({0} AS char), ' ~ ', CAST({1} AS char))",
                minDate, maxDate
        );

        if (cursorEarliestDate != null && cursorId != null) {
            JPQLSubQuery<LocalDate> minDateSubQuery = JPAExpressions.select(qRecruitmentDate.date.min())
                    .from(qRecruitmentDate)
                    .where(qRecruitmentDate.recruitment.eq(qRecruitment));

            booleanBuilder.and(minDateSubQuery.gt(cursorEarliestDate)
                    .or(minDateSubQuery.eq(cursorEarliestDate)
                            .and(qRecruitment.id.lt(cursorId))));
        }

        return queryFactory.select(Projections.constructor(
                DesignerRecruitmentListResponseDto.class,
                qRecruitment.id,
                qRecruitment.title,
                dateRangeExpression,
                qRecruitment.thumbnail,
                reviewCountExpression,
                        // 2. 평균 평점 계산 서브쿼리 (null일 경우 0.0 처리)
                        ExpressionUtils.as(
                                JPAExpressions.select(qReview.rating.avg().coalesce(0.0))
                                        .from(qReview)
                                        .where(qReview.designer.id.eq(designer.getId())),
                                "averageRating"
                        )
        ))
                .from(qRecruitment)
                .join(qRecruitment.recruitmentDates, qRecruitmentDate)
                .where(booleanBuilder)
                .groupBy(qRecruitmentDate.recruitment.id)
                .orderBy(minDate.asc(), qRecruitment.id.desc())
                .limit(size+1)
                .fetch();
    }

    @Override
    public Map<Long, Set<SubCategory>> findSubCategoriesByRecruitmentIds(List<Long> recruitmentIds) {

        EnumPath<SubCategory> sub = Expressions.enumPath(SubCategory.class, "subCategory");

        return queryFactory
                .select(qRecruitment.id, sub)
                .from(qRecruitment)
                .join(qRecruitment.subCategoryList, sub)
                .where(qRecruitment.id.in(recruitmentIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(qRecruitment.id),
                        Collectors.mapping(
                                tuple -> tuple.get(sub),
                                Collectors.toSet()
                        )
                ));
    }


    private List<RecruitmentBasic> fetchRecruitmentList(Long userId, BooleanBuilder where, Coordinate coord, int size, OrderSpecifier<?> order) {
        return queryFactory
                .select(Projections.constructor(RecruitmentBasic.class,
                        qRecruitment.id,
                        qRecruitment.title,
                        qDesigner.user.imageUrl,
                        qDesigner.nickname,
                        qRecruitment.thumbnail,
                        qDesigner.shop,
                        qDesigner.addressLine1,
                        qRecruitment.category,
                        ExpressionUtils.as(getReviewCountSubQuery(), "reviewCount"),
                        ExpressionUtils.as(getDistanceExpression(coord), "distance"),
                        qRecruitmentLike.id.isNotNull(),
                        qRecruitment.createdAt,
                        ExpressionUtils.as(getAverageRatingSubQuery(), "averageRating")
                ))
                .from(qRecruitment)
                .join(qRecruitment.designer, qDesigner)
                .leftJoin(qRecruitmentLike).on(isLikedByMe(userId))
                .where(where)
                .orderBy(order, qRecruitment.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanBuilder buildCommonWhere(SearchCondition cond) {
        BooleanBuilder b = new BooleanBuilder();

        if (cond.keyword() != null && !cond.keyword().isBlank()) {
            b.and(qRecruitment.title.contains(cond.keyword())
                    .or(qRecruitment.content.contains(cond.keyword())));
        }
        if (cond.category() != null) {
            b.and(qRecruitment.category.eq(cond.category()));
            if (cond.subCategory() != null) {
                b.and(qRecruitment.subCategoryList.any().in(cond.subCategory()));
            }
        }

        return b;
    }

    private JPQLSubQuery<Long> getReviewCountSubQuery() {
        return JPAExpressions
                .select(qReview.count())
                .from(qReview)
                .where(qReview.designer.eq(qRecruitment.designer));

    }

    private JPQLSubQuery<Double> getAverageRatingSubQuery() {
        return JPAExpressions.select(qReview.rating.avg().coalesce(0.0))
                .from(qReview)
                .where(qReview.designer.eq(qRecruitment.designer));

    }

    private NumberExpression<Double> getDistanceExpression(Coordinate userCoordinate) {
        if (userCoordinate == null || userCoordinate.latitude() == null) return Expressions.asNumber(0.0).doubleValue();

        String pointWkt = String.format("POINT(%f %f)", userCoordinate.latitude(), userCoordinate.longitude());
        return Expressions.numberTemplate(Double.class,
                "ST_Distance_Sphere(ST_GeomFromText(CONCAT('POINT(', {0}, ' ', {1}, ')'), 4326), ST_GeomFromText({2}, 4326))",
                qDesigner.latitude, qDesigner.longitude, Expressions.constant(pointWkt));
    }

    private BooleanExpression isLikedByMe(Long userId) {
        return qRecruitmentLike.recruitment.id.eq(qRecruitment.id)
                .and(userId != null ? qRecruitmentLike.model.user.id.eq(userId) : qRecruitmentLike.id.isNull());
    }


}
