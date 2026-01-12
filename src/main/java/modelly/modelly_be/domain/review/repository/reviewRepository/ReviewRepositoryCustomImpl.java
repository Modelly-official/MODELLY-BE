package modelly.modelly_be.domain.review.repository.reviewRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.dto.response.MyReviewListResponseDto;
import modelly.modelly_be.domain.review.entity.QReview;
import modelly.modelly_be.domain.review.entity.QReviewImage;
import modelly.modelly_be.domain.user.entity.QDesigner;
import modelly.modelly_be.global.entity.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MyReviewListResponseDto> findReviewList(Long modelId, Category category, Long cursorId, int size) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QReview review = QReview.review;
        QReviewImage reviewImage = QReviewImage.reviewImage; // 리뷰 이미지 테이블
        QDesigner designer = QDesigner.designer;

        booleanBuilder.and(review.model.id.eq(modelId));

        if (category != null) {
            booleanBuilder.and(review.reservation.category.eq(category));
        }

        if (cursorId != null) {
            booleanBuilder.and(review.id.lt(cursorId));
        }

        List<MyReviewListResponseDto> reviews =
                queryFactory
                .select(Projections.constructor(
                        MyReviewListResponseDto.class,
                        review.id,
                        review.designer.nickname,
                        review.designer.shop,
                        review.designer.addressLine1,
                        review.summary,
                        review.rating,
                        review.thumbnail,
                        Expressions.constant(new ArrayList<String>()),
                        review.content,
                        review.createdAt
                ))
                .from(review)
                .join(review.designer, designer)
                .where(booleanBuilder)
                .orderBy(review.id.desc(), review.createdAt.desc())
                .limit(size+1)
                .fetch();


        if (reviews.isEmpty()) return reviews;

        // 2. 리뷰 ID 추출 및 이미지 일괄 조회
        List<Long> reviewIds = reviews.stream()
                .map(MyReviewListResponseDto::reviewId)
                .toList();

        Map<Long, List<String>> imageMap = queryFactory
                .select(reviewImage.review.id, reviewImage.imageUrl)
                .from(reviewImage)
                .where(reviewImage.review.id.in(reviewIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(reviewImage.review.id),
                        Collectors.mapping(tuple -> tuple.get(reviewImage.imageUrl), Collectors.toList())
                ));

        return reviews.stream()
                .map(dto -> dto.withImages(imageMap.getOrDefault(dto.reviewId(), new ArrayList<>())))
                .toList();
    }


}
