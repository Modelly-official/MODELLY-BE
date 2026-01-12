package modelly.modelly_be.domain.like.repository.designerLikeRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.dto.response.LikeDesignerListResponseDto;
import modelly.modelly_be.domain.like.entity.QDesignerLike;
import modelly.modelly_be.domain.review.entity.QReview;
import modelly.modelly_be.domain.user.entity.QDesigner;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

@RequiredArgsConstructor
public class DesignerLikeRepositoryCustomImpl implements DesignerLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<LikeDesignerListResponseDto> findAllByConditions(Long userId, Category category, Long cursorId, int size) {
        QDesigner qDesigner = QDesigner.designer;
        QDesignerLike qDesignerLike = QDesignerLike.designerLike;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.and(qDesignerLike.model.user.id.eq(userId));

        if (category != null) {
            booleanBuilder.and(qDesigner.category.eq(category));
        }

        if (cursorId != null) {
            booleanBuilder.and(qDesignerLike.id.lt(cursorId));
        }

        return queryFactory.select(Projections.constructor(
                LikeDesignerListResponseDto.class,
                qDesignerLike.id,
                qDesigner.id,
                qDesigner.nickname,
                qDesigner.user.imageUrl,
                qDesigner.category,
                qDesigner.shop,
                qDesigner.addressLine1,
                ExpressionUtils.as(getReviewCountSubQuery(), "reviewCount"),
                ExpressionUtils.as(getAverageRatingSubQuery(), "averageRating")
                        ))
                .from(qDesignerLike)
                .join(qDesignerLike.designer, qDesigner)
                .where(booleanBuilder)
                .orderBy(qDesignerLike.id.desc())
                .limit(size+1)
                .fetch();
    }

    private JPQLSubQuery<Long> getReviewCountSubQuery() {
        QReview qReview = QReview.review;
        QDesigner qDesigner = QDesigner.designer;

        return JPAExpressions
                .select(qReview.count())
                .from(qReview)
                .where(qReview.designer.eq(qDesigner));

    }

    private JPQLSubQuery<Double> getAverageRatingSubQuery() {
        QReview qReview = QReview.review;
        QDesigner qDesigner = QDesigner.designer;

        return JPAExpressions.select(qReview.rating.avg().coalesce(0.0))
                .from(qReview)
                .where(qReview.designer.eq(qDesigner));

    }
}
