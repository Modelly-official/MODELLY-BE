package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.home.dto.response.PopularRecruitmentListResponse;
import modelly.modelly_be.domain.like.entity.QRecruitmentLike;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse;
import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentBasic;
import modelly.modelly_be.domain.recruitment.dto.internal.DesignerRecruitmentList;
import modelly.modelly_be.domain.recruitment.entity.QRecruitment;
import modelly.modelly_be.domain.recruitment.entity.QRecruitmentDate;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.domain.reservation.entity.QReservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.domain.review.entity.QReview;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.QDesigner;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.Coordinate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
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
    private static final QReservation qReservation = QReservation.reservation;

    public static final double NEARBY_RADIUS_METER = 5000.0;

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
    public List<DesignerRecruitmentList> findRecruitmentsByDesignerAndDate(Designer designer, YearMonth yearMonth, int size, LocalDate cursorEarliestDate, Long cursorId, RecruitmentStatus status) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qRecruitment.designer.id.eq(designer.getId()));

        booleanBuilder.and(qRecruitmentDate.date.year().eq(yearMonth.getYear())
                .and(qRecruitmentDate.date.month().eq(yearMonth.getMonthValue())));

        if (status != null) {
            booleanBuilder.and(qRecruitment.recruitmentStatus.eq(status));
        }

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
                DesignerRecruitmentList.class,
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

    //내 주위 공고글 조회
    @Override
    public List<RecruitmentBasic> findNearbyRecruitments(
            Long userId,
            Coordinate userCoordinate,
            Category category) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // OPEN 상태인 모집글만
        booleanBuilder.and(qRecruitment.recruitmentStatus.eq(RecruitmentStatus.OPEN));

        // 카테고리 필터
        if (category != null) {
            booleanBuilder.and(qRecruitment.category.eq(category));
        }

        // 거리 조건 (반경 내)
        NumberExpression<Double> distance = getDistanceExpression(userCoordinate);

        booleanBuilder.and(distance.loe(NEARBY_RADIUS_METER));

        List<RecruitmentBasic> content = queryFactory
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
                        ExpressionUtils.as(distance, "distance"),
                        qRecruitmentLike.id.isNotNull(),
                        qRecruitment.createdAt,
                        ExpressionUtils.as(getAverageRatingSubQuery(), "averageRating")
                ))
                .from(qRecruitment)
                .join(qRecruitment.designer, qDesigner)
                .leftJoin(qRecruitmentLike).on(isLikedByMe(userId))
                .where(booleanBuilder)
                .orderBy(distance.asc(), qRecruitment.id.desc()) // 거리순 정렬
                .limit(10)
                .fetch();

        return content;
    }

    @Override
    public List<DesignerProfileResponse.RecruitmentCard> findOpenRecruitmentsByDesigner(Long designerId) {
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
                    return new DesignerProfileResponse.RecruitmentCard(
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

    @Override
    public List<PopularRecruitmentListResponse> findPopularRecruitments(Category category) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        //OPEN 상태인 모집글만
        booleanBuilder.and(qRecruitment.recruitmentStatus.eq(RecruitmentStatus.OPEN));

        //카테고리 필터
        if (category != null) {
            booleanBuilder.and(qRecruitment.category.eq(category));
        }

        // 서브쿼리: 평균 평점
        NumberExpression<Double> averageRating = Expressions.asNumber(getAverageRatingSubQuery()).doubleValue();

        // 최근 30일 내 예약/리뷰에 더 높은 가중치
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // 최근 예약 수
        JPQLSubQuery<Long> recentReservationCountSubQuery = JPAExpressions
                .select(qReservation.count())
                .from(qReservation)
                .where(qReservation.recruitment.eq(qRecruitment)
                        .and(qReservation.status.in(ReservationStatus.RESERVATION_CONFIRMED, ReservationStatus.RESERVATION_PENDING))
                        .and(qReservation.createdAt.after(thirtyDaysAgo)));

        NumberExpression<Double> recentReservationCount = Expressions.asNumber(recentReservationCountSubQuery)
                .coalesce(0L).doubleValue();

        // 최근 리뷰 수
        JPQLSubQuery<Long> recentReviewCountSubQuery = JPAExpressions
                .select(qReview.count())
                .from(qReview)
                .where(qReview.designer.eq(qRecruitment.designer)
                        .and(qReview.createdAt.after(thirtyDaysAgo)));

        NumberExpression<Double> recentReviewCount = Expressions.asNumber(recentReviewCountSubQuery)
                .coalesce(0L).doubleValue();

        //최근 찜 수
        JPQLSubQuery<Long> recentLikeCountSubQuery = JPAExpressions
                .select(qRecruitmentLike.count())
                .from(qRecruitmentLike)
                .where(qRecruitmentLike.recruitment.eq(qRecruitment)
                        .and(qRecruitmentLike.createdAt.after(thirtyDaysAgo)));

         NumberExpression<Double> recentLikeCount = Expressions.asNumber(recentLikeCountSubQuery).doubleValue();

        // 인기도 점수: 최근 활동에 2배 가중치
        NumberExpression<Double> popularityScore =
                //recentReservationCount.multiply(6.0)  // 최근 예약 × 6
                        qRecruitment.reservationCount.doubleValue().multiply(2.0)  // 전체 예약 × 2
                        //.add(recentReviewCount.multiply(4.0))  // 최근 리뷰 × 4
                        .add(qRecruitment.designer.reviewCount.doubleValue().multiply(1.5))        // 전체 리뷰 × 1.5
                        .add(qRecruitment.likeCount.doubleValue().multiply(1.5)) // 전체 찜 × 1.5
                        //.add(recentLikeCount.multiply(1.5)) //최근 찜 x 3
                        .add(averageRating.multiply(2.0));    // 평점 × 2

        List<Tuple> tuples = queryFactory
                .select(
                        qRecruitment.id,
                        qDesigner.nickname,
                        qDesigner.shop,
                        qRecruitment.title,
                        qRecruitment.category,
                        popularityScore
                )
                .from(qRecruitment)
                .join(qRecruitment.designer, qDesigner)
                .where(booleanBuilder)
                .orderBy(popularityScore.desc(), qRecruitment.id.desc())
                .limit(5)
                .fetch();

        List<Long> recruitmentIds = tuples.stream()
                .map(t -> t.get(qRecruitment.id))
                .distinct()
                .collect(Collectors.toList());

        Map<Long, List<String>> subCategories = fetchSubCategoriesByRecruitmentIds(recruitmentIds);

        List<PopularRecruitmentListResponse> results = new ArrayList<>();

        for (Tuple t : tuples) {
            Long id = t.get(qRecruitment.id);
            String nickname = t.get(qDesigner.nickname);
            String shop = t.get(qDesigner.shop);
            String title = t.get(qRecruitment.title);
            Category categoryVal = t.get(qRecruitment.category);
            Double score = t.get(popularityScore);

            List<String> subList = subCategories.getOrDefault(id, List.of());

            results.add(new PopularRecruitmentListResponse(
                    id,
                    nickname,
                    shop,
                    title,
                    categoryVal,
                    subList,
                    score
            ));
        }


        return results;
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

        b.and(qRecruitment.recruitmentStatus.eq(RecruitmentStatus.OPEN));

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
