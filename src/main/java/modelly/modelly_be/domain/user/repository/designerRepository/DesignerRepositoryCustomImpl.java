package modelly.modelly_be.domain.user.repository.designerRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.entity.QDesignerLike;
import modelly.modelly_be.domain.map.dto.response.ShopResponse;
import modelly.modelly_be.domain.portfolio.entity.QPortfolio;
import modelly.modelly_be.domain.review.entity.QReview;
import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.domain.user.entity.QDesigner;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.utils.Coordinate;
import modelly.modelly_be.global.utils.SearchCondition;

import java.util.List;

@RequiredArgsConstructor
public class DesignerRepositoryCustomImpl implements DesignerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QDesigner qDesigner = QDesigner.designer;
    private static final QPortfolio qPortfolio = QPortfolio.portfolio;
    private static final QReview qReview = QReview.review;
    private static final QDesignerLike qDesignerLike = QDesignerLike.designerLike;

    @Override
    public List<DesignerListResponseDto> findDesignersByCreatedAt(Long userId, SearchCondition searchCondition, Long cursorId, int size, Coordinate userCoordinate) {

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        if (cursorId != null) {
            booleanBuilder.and(qDesigner.id.lt(cursorId));
        }

        return fetchDesignerList(userId, booleanBuilder, userCoordinate, size, qDesigner.createdAt.desc());
    }

    @Override
    public List<DesignerListResponseDto> findDesignersByReviews(Long userId, SearchCondition searchCondition, Long cursorId, Long cursorReviewCount, int size, Coordinate userCoordinate) {

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        NumberExpression<Long> reviewCount = Expressions.asNumber(getReviewCountSubQuery());

        if (cursorId != null && cursorReviewCount != null) {
            booleanBuilder.and(
                    reviewCount.lt(cursorReviewCount)
                            .or(reviewCount.eq(cursorReviewCount)
                                    .and(qDesigner.id.lt(cursorId)))
            );
        }

        return fetchDesignerList(userId, booleanBuilder, userCoordinate, size, reviewCount.desc());
    }

    @Override
    public List<DesignerListResponseDto> findDesignersByDistance(Long userId, SearchCondition searchCondition, Long cursorId, Double cursorDistance, int size, Coordinate userCoordinate) {

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        NumberExpression<Double> distance = getDistanceExpression(userCoordinate);

        if (cursorId != null && cursorDistance != null) {
            booleanBuilder.and(
                    distance.gt(cursorDistance)
                            .or(distance.eq(cursorDistance)
                                    .and(qDesigner.id.lt(cursorId)))
            );
        }

        return fetchDesignerList(userId, booleanBuilder, userCoordinate, size, distance.asc());
    }

    @Override
    public List<ShopResponse> findShopsByDistance(Long userId, Category category, int size, Coordinate userCoordinate) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (category != null) {
            booleanBuilder.and(qDesigner.category.eq(category));
        }

        if (userCoordinate == null || userCoordinate.latitude() == null || userCoordinate.longitude() == null) {
            throw new GeneralException(ErrorStatus.COORDINATE_BAD_REQUEST);
        }

        List<ShopResponse> dtos = queryFactory
                .select(Projections.constructor(
                        ShopResponse.class,
                        qDesigner.id,
                        qDesigner.nickname,
                        qDesigner.category,
                        qDesigner.latitude,
                        qDesigner.longitude
                ))
                .from(qDesigner)
                .leftJoin(qDesignerLike).on(isLikedByMe(userId))
                .where(booleanBuilder)
                .orderBy(getDistanceExpression(userCoordinate).asc(), qDesigner.id.desc())
                .limit(size)
                .fetch();

        return dtos;
    }


    private BooleanBuilder buildCommonWhere(SearchCondition searchCondition) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (searchCondition.keyword() != null && !searchCondition.keyword().isEmpty()) {
            booleanBuilder.and(qDesigner.nickname.eq(searchCondition.keyword())
                    .or(qDesigner.shop.eq(searchCondition.keyword())));
        }

        if (searchCondition.category() != null) {
            booleanBuilder.and(qDesigner.category.eq(searchCondition.category()));
        }

        return booleanBuilder;
    }

    private List<DesignerListResponseDto> fetchDesignerList(Long userId, BooleanBuilder where, Coordinate userCoordinate, int size, OrderSpecifier<?> primaryOrder) {

        JPQLSubQuery<String> firstImgSub = JPAExpressions
                .select(qPortfolio.thumbnail.max())
                .from(qPortfolio)
                .where(qPortfolio.designer.eq(qDesigner));

        StringExpression portfolio = Expressions.asString(ExpressionUtils.as(firstImgSub, "thumbnail"));

        return queryFactory
                .select(Projections.constructor(DesignerListResponseDto.class,
                        qDesigner.id,
                        qDesigner.nickname,
                        qDesigner.shop,
                        qDesigner.addressLine1,
                        portfolio,
                        qDesigner.category,
                        ExpressionUtils.as(getReviewCountSubQuery(), "reviewCount"),
                        ExpressionUtils.as(getDistanceExpression(userCoordinate), "distance"),
                        qDesignerLike.id.isNotNull(),
                        qDesigner.createdAt,
                        ExpressionUtils.as(getAverageRatingSubQuery(), "averageRating")
                ))
                .from(qDesigner)
                .leftJoin(qDesignerLike).on(isLikedByMe(userId))
                .where(where)
                .orderBy(primaryOrder, qDesigner.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private JPQLSubQuery<Long> getReviewCountSubQuery() {
        return JPAExpressions
                .select(qReview.count())
                .from(qReview)
                .where(qReview.designer.eq(qDesigner));

    }

    private JPQLSubQuery<Double> getAverageRatingSubQuery() {
        return JPAExpressions.select(qReview.rating.avg().coalesce(0.0))
                .from(qReview)
                .where(qReview.designer.eq(qDesigner));

    }

    private NumberExpression<Double> getDistanceExpression(Coordinate userCoordinate) {
        if (userCoordinate == null || userCoordinate.latitude() == null) {

            return Expressions.numberTemplate(Double.class, "NULL");
        }

        String pointWkt = String.format("POINT(%f %f)", userCoordinate.latitude(), userCoordinate.longitude());
        return Expressions.numberTemplate(Double.class,
                "ST_Distance_Sphere(ST_GeomFromText(CONCAT('POINT(', {0}, ' ', {1}, ')'), 4326), ST_GeomFromText({2}, 4326))",
                qDesigner.latitude, qDesigner.longitude, Expressions.constant(pointWkt));
    }

    private BooleanExpression isLikedByMe(Long userId) {
        return qDesignerLike.designer.id.eq(qDesigner.id)
                .and(userId != null ? qDesignerLike.model.user.id.eq(userId) : qDesignerLike.id.isNull());
    }

}
