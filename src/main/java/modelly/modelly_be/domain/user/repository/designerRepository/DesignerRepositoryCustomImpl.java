package modelly.modelly_be.domain.user.repository.designerRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
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

    @Override
    public List<DesignerListResponseDto> findDesignersByCreatedAt(Long userId, SearchCondition searchCondition, Long cursorId, int size) {
        QDesigner qDesigner = QDesigner.designer;
        QPortfolio qPortfolio = QPortfolio.portfolio;
        QDesignerLike qDesignerLike = QDesignerLike.designerLike;
        QReview qReview = QReview.review;

        StringPath thumbnail = Expressions.stringPath("thumbnail");
        JPQLSubQuery<String> firstImgSub = JPAExpressions
                .select(qPortfolio.imageUrl)
                .from(qPortfolio)
                .where(qPortfolio.designer.eq(qDesigner))
                .orderBy(qPortfolio.createdAt.desc())
                .limit(1);

        StringExpression portfolio = Expressions.asString(ExpressionUtils.as(firstImgSub, thumbnail));
        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        //기획에게 디자이너 최신 가입순관련 논의 후 수정하기
        if (cursorId != null) {
            booleanBuilder.and(qDesigner.id.lt(cursorId));
        }

        List<DesignerListResponseDto> dtos = queryFactory
                .select(Projections.constructor(
                        DesignerListResponseDto.class,
                        qDesigner.id,
                        qDesigner.nickname,
                        qDesigner.shop,
                        qDesigner.addressLine1,
                        portfolio,
                        qDesigner.category,
                        Expressions.nullExpression(Long.class),
                        Expressions.nullExpression(Double.class),
                        qDesignerLike.id.isNotNull(),
                        qDesigner.createdAt,
                        // 2. 평균 평점 계산 서브쿼리 (null일 경우 0.0 처리)
                        ExpressionUtils.as(
                                JPAExpressions.select(qReview.rating.avg().coalesce(0.0))
                                        .from(qReview)
                                        .where(qReview.designer.eq(qDesigner)),
                                "averageRating"
                        )
                ))
                .from(qDesigner)
                .leftJoin(qDesignerLike).on(qDesignerLike.designer.id.eq(qDesigner.id)
                        .and(userId !=null ? qDesignerLike.model.user.id.eq(userId) : null))
                .where(booleanBuilder)
                .orderBy(qDesigner.createdAt.desc(), qDesigner.id.desc())
                .limit(size+1)
                .fetch();

        return dtos;
    }

    @Override
    public List<DesignerListResponseDto> findDesignersByReviews(Long userId, SearchCondition searchCondition, Long cursorId, Long cursorReviewCount, int size) {
        QDesigner qDesigner = QDesigner.designer;
        QPortfolio qPortfolio = QPortfolio.portfolio;
        QDesignerLike qDesignerLike = QDesignerLike.designerLike;
        QReview qReview = QReview.review;

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        StringPath thumbnail = Expressions.stringPath("thumbnail");
        JPQLSubQuery<String> firstImgSub = JPAExpressions
                .select(qPortfolio.imageUrl)
                .from(qPortfolio)
                .where(qPortfolio.designer.eq(qDesigner))
                .orderBy(qPortfolio.createdAt.desc())
                .limit(1);

        StringExpression portfolio = Expressions.asString(ExpressionUtils.as(firstImgSub, thumbnail));

        NumberPath<Long> reviewCount = Expressions.numberPath(Long.class, "reviewCount");
        JPQLSubQuery<Long> reviewCountSubQuery = JPAExpressions
                .select(qReview.count())
                .from(qReview)
                .where(qReview.designer.eq(qDesigner));

        NumberExpression<Long> reviewCountExpression = Expressions.asNumber(ExpressionUtils.as(reviewCountSubQuery, reviewCount));

        if (cursorId != null && cursorReviewCount != null) {
            booleanBuilder.and(
                    reviewCount.lt(cursorReviewCount)
                            .or(reviewCount.eq(cursorReviewCount)
                                    .and(qDesigner.id.lt(cursorId)))
            );
        }

        List<DesignerListResponseDto> dtos = queryFactory
                .select(Projections.constructor(
                        DesignerListResponseDto.class,
                        qDesigner.id,
                        qDesigner.nickname,
                        qDesigner.shop,
                        qDesigner.addressLine1,
                        portfolio,
                        qDesigner.category,
                        reviewCountExpression,
                        Expressions.nullExpression(Double.class),
                        qDesignerLike.id.isNotNull(),
                        qDesigner.createdAt,
                        // 2. 평균 평점 계산 서브쿼리 (null일 경우 0.0 처리)
                        ExpressionUtils.as(
                                JPAExpressions.select(qReview.rating.avg().coalesce(0.0))
                                        .from(qReview)
                                        .where(qReview.designer.eq(qDesigner)),
                                "averageRating"
                        )
                ))
                .from(qDesigner)
                .leftJoin(qDesignerLike).on(qDesignerLike.designer.id.eq(qDesigner.id)
                        .and(userId !=null ? qDesignerLike.model.user.id.eq(userId) : null))
                .where(booleanBuilder)
                .orderBy(reviewCount.desc(), qDesigner.id.desc())
                .limit(size+1)
                .fetch();

        return dtos;
    }

    @Override
    public List<DesignerListResponseDto> findDesignersByDistance(Long userId, SearchCondition searchCondition, Long cursorId, Double cursorDistance, int size, Coordinate userCoordinate) {
        QDesigner qDesigner = QDesigner.designer;
        QPortfolio qPortfolio = QPortfolio.portfolio;
        QDesignerLike qDesignerLike = QDesignerLike.designerLike;
        QReview qReview = QReview.review;

        StringPath thumbnail = Expressions.stringPath("thumbnail");
        JPQLSubQuery<String> firstImgSub = JPAExpressions
                .select(qPortfolio.imageUrl)
                .from(qPortfolio)
                .where(qPortfolio.designer.eq(qDesigner))
                .orderBy(qPortfolio.createdAt.desc())
                .limit(1);

        StringExpression portfolio = Expressions.asString(ExpressionUtils.as(firstImgSub, thumbnail));

        BooleanBuilder booleanBuilder = buildCommonWhere(searchCondition);

        // MySQL ST_Distance_Sphere with SRID 4326: 실제 테스트 결과 POINT(latitude, longitude) 순서 사용
        String pointWkt = String.format("POINT(%f %f)",
                userCoordinate.latitude(), userCoordinate.longitude());

        NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
                "ST_Distance_Sphere(ST_GeomFromText(CONCAT('POINT(', {0}, ' ', {1}, ')'), 4326), ST_GeomFromText({2}, 4326))",
                qDesigner.latitude,
                qDesigner.longitude,
                Expressions.constant(pointWkt)
        );

        if (cursorId != null && cursorDistance != null) {
            booleanBuilder.and(
                    distance.gt(cursorDistance)
                            .or(distance.eq(cursorDistance)
                                    .and(qDesigner.id.lt(cursorId)))
            );
        }

        List<DesignerListResponseDto> dtos = queryFactory
                .select(Projections.constructor(
                        DesignerListResponseDto.class,
                        qDesigner.id,
                        qDesigner.nickname,
                        qDesigner.shop,
                        qDesigner.addressLine1,
                        portfolio,
                        qDesigner.category,
                        Expressions.nullExpression(Long.class),
                        distance,
                        qDesignerLike.id.isNotNull(),
                        qDesigner.createdAt,
                        // 2. 평균 평점 계산 서브쿼리 (null일 경우 0.0 처리)
                        ExpressionUtils.as(
                                JPAExpressions.select(qReview.rating.avg().coalesce(0.0))
                                        .from(qReview)
                                        .where(qReview.designer.eq(qDesigner)),
                                "averageRating"
                        )
                ))
                .from(qDesigner)
                .leftJoin(qDesignerLike).on(qDesignerLike.designer.id.eq(qDesigner.id)
                        .and(userId !=null ? qDesignerLike.model.user.id.eq(userId) : null))
                .where(booleanBuilder)
                .orderBy(distance.asc(), qDesigner.id.desc())
                .limit(size+1)
                .fetch();

        return dtos;
    }

    @Override
    public List<ShopResponse> findShopsByDistance(Long userId, Category category, int size, Coordinate userCoordinate) {
        QDesigner qDesigner = QDesigner.designer;
        QDesignerLike qDesignerLike = QDesignerLike.designerLike;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (category != null) {
            booleanBuilder.and(qDesigner.category.eq(category));
        }

        if (userCoordinate == null || userCoordinate.latitude() == null || userCoordinate.longitude() == null) {
            throw new GeneralException(ErrorStatus.COORDINATE_BAD_REQUEST);
        }

        // MySQL ST_Distance_Sphere with SRID 4326: 실제 테스트 결과 POINT(latitude, longitude) 순서 사용
        String pointWkt = String.format("POINT(%f %f)",
                userCoordinate.latitude(), userCoordinate.longitude());

        NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
                "ST_Distance_Sphere(ST_GeomFromText(CONCAT('POINT(', {0}, ' ', {1}, ')'), 4326), ST_GeomFromText({2}, 4326))",
                qDesigner.latitude,
                qDesigner.longitude,
                Expressions.constant(pointWkt)
        );

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
                .leftJoin(qDesignerLike).on(qDesignerLike.designer.id.eq(qDesigner.id)
                        .and(userId !=null ? qDesignerLike.model.user.id.eq(userId) : null))
                .where(booleanBuilder)
                .orderBy(distance.asc(), qDesigner.id.desc())
                .limit(size)
                .fetch();

        return dtos;
    }


    private BooleanBuilder buildCommonWhere(SearchCondition searchCondition) {
        QDesigner qDesigner = QDesigner.designer;
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

}
