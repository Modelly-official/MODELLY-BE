package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.like.entity.QRecruitmentLike;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.entity.QRecruitment;
import modelly.modelly_be.domain.recruitment.entity.QRecruitmentDate;
import modelly.modelly_be.domain.review.entity.QReview;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.QDesigner;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.Coordinate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RecruitmentRepositoryCustomImpl implements RecruitmentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private BooleanBuilder buildCommonWhere(SearchCondition cond) {
        QRecruitment r = QRecruitment.recruitment;
        BooleanBuilder b = new BooleanBuilder();

        if (cond.keyword() != null && !cond.keyword().isBlank()) {
            b.and(r.title.contains(cond.keyword())
                    .or(r.content.contains(cond.keyword())));
        }
        if (cond.category() != null) {
            b.and(r.category.eq(cond.category()));
            if (cond.subCategory() != null) {
                b.and(r.subCategory.eq(cond.subCategory()));
            }
        }

        // 기타 조건 추가: 날짜 필터 등

        return b;
    }

    @Override
    public List<RecruitmentListResponseDto> findRecruitmentsByCreatedAt(Long userId, SearchCondition searchCondition, Long cursorId, int size) {
        QRecruitment qRecruitment = QRecruitment.recruitment;
        QDesigner qDesigner = QDesigner.designer;
        QRecruitmentLike qRecruitmentLike = QRecruitmentLike.recruitmentLike;

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);
        if (cursorId != null) {
            booleanBuilder.and(qRecruitment.id.lt(cursorId));
        }

        List<RecruitmentListResponseDto> dtos = queryFactory
                .select(Projections.constructor(
                        RecruitmentListResponseDto.class,
                        qRecruitment.id,
                        qRecruitment.title,
                        qDesigner.user.imageUrl,
                        qDesigner.nickname,
                        qRecruitment.thumbnail,
                        qDesigner.shop,
                        qDesigner.addressLine1,
                        qRecruitment.category,
                        qRecruitment.subCategory,
                        Expressions.nullExpression(Long.class),
                        Expressions.nullExpression(Double.class),
                        qRecruitmentLike.id.isNotNull(),
                        qRecruitment.createdAt
                ))
                .from(qRecruitment)
                .join(qRecruitment.designer, qDesigner)
                .leftJoin(qRecruitmentLike).on(qRecruitmentLike.recruitment.id.eq(qRecruitment.id)
                        .and(userId != null ? qRecruitmentLike.model.user.id.eq(userId) : null)) //userId가 not null일때만
                .where(booleanBuilder)
                .orderBy(qRecruitment.createdAt.desc(), qRecruitment.id.desc())
                .limit(size+1)
                .fetch();

        return dtos;
    }

    @Override
    public List<RecruitmentListResponseDto> findRecruitmentsByReviews(Long userId, SearchCondition searchCondition, Long cursorId, Long cursorReviewCount, int size) {
        QRecruitment qRecruitment = QRecruitment.recruitment;
        QDesigner qDesigner = QDesigner.designer;
        QRecruitmentLike qRecruitmentLike = QRecruitmentLike.recruitmentLike;
        QReview qReview = QReview.review;

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        NumberPath<Long> reviewCount = Expressions.numberPath(Long.class, "reviewCount");
        JPQLSubQuery<Long> reviewCountSubQuery=JPAExpressions
                .select(qReview.count())
                .from(qReview)
                .where(qReview.designer.eq(qRecruitment.designer));

        NumberExpression<Long> reviewCountExpression = Expressions.asNumber(ExpressionUtils.as(reviewCountSubQuery, reviewCount));

        if (cursorId != null && cursorReviewCount != null) {
            booleanBuilder.and(
                    reviewCount.lt(cursorReviewCount)
                            .or(reviewCount.eq(cursorReviewCount)
                                    .and(qRecruitment.id.lt(cursorId)))
            );
        }

        List<RecruitmentListResponseDto> dtos = queryFactory
                .select(Projections.constructor(
                        RecruitmentListResponseDto.class,
                        qRecruitment.id,
                        qRecruitment.title,
                        qDesigner.user.imageUrl,
                        qDesigner.nickname,
                        qRecruitment.thumbnail,
                        qDesigner.shop,
                        qDesigner.addressLine1,
                        qRecruitment.category,
                        qRecruitment.subCategory,
                        reviewCountExpression,
                        Expressions.nullExpression(Double.class),
                        qRecruitmentLike.id.isNotNull(),
                        qRecruitment.createdAt
                ))
                .from(qRecruitment)
                .join(qRecruitment.designer, qDesigner)
                .leftJoin(qRecruitmentLike).on(qRecruitmentLike.recruitment.id.eq(qRecruitment.id)
                        .and(userId != null ? qRecruitmentLike.model.user.id.eq(userId) : null)) //userId가 not null일때만
                .where(booleanBuilder)
                .orderBy(reviewCount.desc(), qRecruitment.id.desc())
                .limit(size+1)
                .fetch();

        return dtos;
    }

    @Override
    public List<RecruitmentListResponseDto> findRecruitmentsByDistance(Long userId, SearchCondition searchCondition, Long cursorId, Double cursorDistance, int size, Coordinate userCoordinate) {
        QRecruitment qRecruitment = QRecruitment.recruitment;
        QDesigner qDesigner = QDesigner.designer;
        QRecruitmentLike qRecruitmentLike = QRecruitmentLike.recruitmentLike;

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        if (userCoordinate == null || userCoordinate.latitude() == null || userCoordinate.longitude() == null) {
            throw new IllegalArgumentException("거리 정렬 시 사용자 좌표 필요");
        }

        // 기준점: WGS-84 + SRID 4326
        String pointWkt = String.format("POINT(%f %f)",
                userCoordinate.longitude(), userCoordinate.latitude());

        NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
                "ST_Distance_Sphere(ST_GeomFromText(CONCAT('POINT(', {0}, ' ', {1}, ')'), 4326), ST_GeomFromText({2}, 4326))",
                qRecruitment.designer.longitude,
                qRecruitment.designer.latitude,
                Expressions.constant(pointWkt)
        );


        if (cursorId != null && cursorDistance != null) {
            booleanBuilder.and(
                    distance.gt(cursorDistance)
                            .or(distance.eq(cursorDistance)
                                    .and(qRecruitment.id.lt(cursorId)))
            );
        }

        List<RecruitmentListResponseDto> dtos = queryFactory
                .select(
                        Projections.constructor(
                                RecruitmentListResponseDto.class,
                                qRecruitment.id,
                                qRecruitment.title,
                                qDesigner.user.imageUrl,
                                qDesigner.nickname,
                                qRecruitment.thumbnail,
                                qDesigner.shop,
                                qDesigner.addressLine1,
                                qRecruitment.category,
                                qRecruitment.subCategory,
                                Expressions.nullExpression(Long.class),
                                distance,
                                qRecruitmentLike.id.isNotNull(),
                                qRecruitment.createdAt
                        ))
                .from(qRecruitment)
                .join(qRecruitment.designer, qDesigner)
                .leftJoin(qRecruitmentLike).on(qRecruitmentLike.recruitment.id.eq(qRecruitment.id)
                        .and(userId != null ? qRecruitmentLike.model.user.id.eq(userId) : null)) //userId가 not null일때만
                .where(booleanBuilder)
                .orderBy(distance.asc(), qRecruitment.id.desc())
                .limit(size+1)
                .fetch();

        return dtos;
    }

    @Override
    public List<DesignerRecruitmentListResponseDto> findRecruitmentsByDesignerAndDate(Designer designer, String month, int size, LocalDate cursorEarliestDate, Long cursorId) {
        QRecruitment qRecruitment = QRecruitment.recruitment;
        QRecruitmentDate qRecruitmentDate = QRecruitmentDate.recruitmentDate;

        YearMonth yearMonth = YearMonth.parse(month);

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qRecruitment.designer.id.eq(designer.getId()));

        booleanBuilder.and(qRecruitmentDate.date.year().eq(yearMonth.getYear())
                .and(qRecruitmentDate.date.month().eq(yearMonth.getMonthValue())));



        DatePath<LocalDate> earliestRecruitmentDate = Expressions.datePath(LocalDate.class, "earliestRecruitmentDate");

        JPQLSubQuery<LocalDate> earliestDate = JPAExpressions.select(qRecruitmentDate.date.min())
                .from(QRecruitmentDate.recruitmentDate)
                .where(QRecruitmentDate.recruitmentDate.recruitment.eq(qRecruitment)
                        .and(QRecruitmentDate.recruitmentDate.date.year().eq(yearMonth.getYear())
                                .and(QRecruitmentDate.recruitmentDate.date.month().eq(yearMonth.getMonthValue()))));

        DateExpression<LocalDate> earliestDateExpression = Expressions.asDate(ExpressionUtils.as(earliestDate, earliestRecruitmentDate));

        if (cursorEarliestDate != null && cursorId != null) {
            booleanBuilder.and(earliestDate.gt(cursorEarliestDate)
                    .or(earliestDate.eq(cursorEarliestDate)
                            .and(qRecruitmentDate.recruitment.id.lt(cursorId))));
        }

        return queryFactory.select(Projections.constructor(
                DesignerRecruitmentListResponseDto.class,
                qRecruitment.id,
                qRecruitment.title,
                earliestDateExpression
        ))
                .from(qRecruitment)
                .join(qRecruitment.recruitmentDates, qRecruitmentDate)
                .where(booleanBuilder)
                .groupBy(qRecruitmentDate.recruitment.id)
                .orderBy(earliestRecruitmentDate.asc(), qRecruitment.id.desc())
                .limit(size+1)
                .fetch();
    }

}
