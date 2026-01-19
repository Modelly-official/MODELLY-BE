package modelly.modelly_be.domain.like.repository.recruitmentLikeRepository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.dto.response.LikeRecruitmentListResponseDto;
import modelly.modelly_be.domain.like.entity.QRecruitmentLike;
import modelly.modelly_be.domain.recruitment.entity.QRecruitment;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.review.entity.QReview;
import modelly.modelly_be.domain.user.entity.QDesigner;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RecruitmentLikeRepositoryCustomImpl implements RecruitmentLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<LikeRecruitmentListResponseDto> findAllByConditions(Long userId, Category category, Long cursorId, int size){
        QRecruitment qRecruitment = QRecruitment.recruitment;
        QRecruitmentLike qRecruitmentLike = QRecruitmentLike.recruitmentLike;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.and(qRecruitmentLike.model.user.id.eq(userId));

        if (category != null) {
            booleanBuilder.and(qRecruitment.category.eq(category));
        }

        if (cursorId != null) {
            booleanBuilder.and(qRecruitmentLike.id.lt(cursorId));
        }

        List<LikeRecruitmentListResponseDto> recruitments = queryFactory.select(Projections.constructor(
                LikeRecruitmentListResponseDto.class,
                qRecruitmentLike.id,
                qRecruitment.id,
                qRecruitment.title,
                qRecruitment.designer.user.imageUrl,
                qRecruitment.designer.user.name,
                qRecruitment.thumbnail,
                qRecruitment.designer.shop,
                qRecruitment.designer.addressLine1,
                qRecruitment.designer.category,
                Expressions.constant(new ArrayList<String>()),
                ExpressionUtils.as(getReviewCountSubQuery(), "reviewCount"),
                ExpressionUtils.as(getAverageRatingSubQuery(), "averageRating"),
                qRecruitmentLike.createdAt
                        ))
                .from(qRecruitmentLike)
                .join(qRecruitmentLike.recruitment, qRecruitment)
                .where(booleanBuilder)
                .orderBy(qRecruitmentLike.id.desc())
                .limit(size+1)
                .fetch();

        if (recruitments.isEmpty()) return recruitments;

        List<Long> recruitmentIds = recruitments.stream()
                .map(LikeRecruitmentListResponseDto::recruitmentId)
                .collect(Collectors.toList());

        List<Recruitment> recruitmentsWithSubCategories = queryFactory
                .selectFrom(qRecruitment)
                .where(qRecruitment.id.in(recruitmentIds))
                .fetch();

        Map<Long, List<SubCategory>> subCategoryMap = recruitmentsWithSubCategories.stream()
                .collect(Collectors.toMap(
                        Recruitment::getId,
                        r -> r.getSubCategoryList().stream()
                                .collect(Collectors.toUnmodifiableList())
                ));

        recruitments.forEach(dto -> {
            List<SubCategory> subCategories = subCategoryMap.getOrDefault(dto.recruitmentId(),new ArrayList<>());
            dto.withSubCategories(subCategories);
        });

        return recruitments;
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
