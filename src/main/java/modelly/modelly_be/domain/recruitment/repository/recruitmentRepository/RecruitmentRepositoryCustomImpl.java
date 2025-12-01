package modelly.modelly_be.domain.recruitment.repository.recruitmentRepository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.entity.QRecruitmentLike;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.entity.QRecruitment;
import modelly.modelly_be.domain.recruitment.entity.QRecruitmentImage;
import modelly.modelly_be.domain.review.entity.QReview;
import modelly.modelly_be.domain.user.entity.QDesigner;
import modelly.modelly_be.global.entity.SortOption;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.UserCoordinate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@RequiredArgsConstructor
public class RecruitmentRepositoryCustomImpl implements RecruitmentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<RecruitmentListResponseDto> findRecruitmentsByConditions(Long userId, SearchCondition searchCondition, SortOption sortOption, Long cursorId, int size, UserCoordinate userCoordinate) {
        QRecruitment qRecruitment = QRecruitment.recruitment;
        QDesigner qDesigner = QDesigner.designer;
        QRecruitmentImage qRecruitmentImage = QRecruitmentImage.recruitmentImage;
        QRecruitmentLike qRecruitmentLike = QRecruitmentLike.recruitmentLike;
        QReview qReview = QReview.review;
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        //검색
        if (searchCondition.keyword() != null && !searchCondition.keyword().isBlank()) {
            booleanBuilder.and(
                    qRecruitment.title.contains(searchCondition.keyword())
                            .or(qRecruitment.content.contains(searchCondition.keyword()))
            );
        } //추후 띄어쓰기, 오타 등 유연하게 처리해야함

        //카테고리
        if (searchCondition.category() != null) {
            booleanBuilder.and(qRecruitment.category.eq(searchCondition.category()));
            if (searchCondition.subCategory() != null) {
                booleanBuilder.and(qRecruitment.subCategory.eq(searchCondition.subCategory()));
            }
        }
        //cursorId
        if (cursorId != null) {
            if (sortOption != null) {
                switch (sortOption) {
                    case NEWEST:
                        booleanBuilder.and(qRecruitment.id.lt(cursorId));
                    case DISTANCE:
                        //query.orderBy(qRecruitment.createdAt.desc()); //추후 거리순으로 수정
                        break;
                    case MOST_REVIEWS:
//                    JPAQuery<Long> reviewCountSubQuery = JPAExpressions
//                            .select(qReview.count())
//                            .from(qReview
//                            ).where(qReview.designer.id.eq(qDesigner.id));
//
//                    query.orderBy(reviewCountSubQuery.desc(), qRecruitment.id.desc()); //추후 리뷰 많은순으로 수정
                        break;
                }
            } else {
                booleanBuilder.and(qRecruitment.id.lt(cursorId));
            }
        }

        //날짜 필터링

        //공고 썸네일 (대표이미지) 서브쿼리로 불러오기
        QRecruitmentImage subImage = new QRecruitmentImage(qRecruitmentImage);

        var query = queryFactory.select(
                        Projections.constructor(
                                RecruitmentListResponseDto.class,
                                qRecruitment.id,
                                qRecruitment.title,
                                qDesigner.user.imageUrl,
                                qDesigner.nickname,
                                qRecruitmentImage.imageUrl, //썸네이 문제 어떻게 할지
                                qDesigner.shop,
                                qDesigner.addressLine1,
                                qRecruitment.category,
                                qRecruitment.subCategory,
                                qRecruitmentLike.id.isNotNull(),
                                qRecruitment.createdAt
                        ))
                .from(qRecruitment)
                .join(qRecruitment.designer,qDesigner)
                .leftJoin(qRecruitment.recruitmentImages, qRecruitmentImage)
                .leftJoin(qRecruitmentLike)
                .on(qRecruitmentLike.recruitment.id.eq(qRecruitment.id))
                .where(booleanBuilder)
                .distinct();


        //정렬
        if (sortOption != null) {
            switch (sortOption) {
                case NEWEST :
                    query.orderBy(qRecruitment.createdAt.desc());
                    break;
                case DISTANCE:
                    query.orderBy(qRecruitment.createdAt.desc()); //추후 거리순으로 수정


                    break;
                case MOST_REVIEWS:
//                    JPAQuery<Long> reviewCountSubQuery = JPAExpressions
//                            .select(qReview.count())
//                            .from(qReview
//                            ).where(qReview.designer.id.eq(qDesigner.id));
//
//                    query.orderBy(reviewCountSubQuery.desc(), qRecruitment.id.desc()); //추후 리뷰 많은순으로 수정
                    break;
            }
        } else {
            // 기본 정렬: 최신순
            query.orderBy(qRecruitment.createdAt.desc());
        }



        List<RecruitmentListResponseDto> dtos = query
                .limit(size+1)
                .fetch();

        boolean hasNext = dtos.size() > size;

        List<RecruitmentListResponseDto> sliceContent = hasNext
                ? dtos.subList(0, size)
                : dtos;

        return new SliceImpl<>(sliceContent, PageRequest.of(0, size), hasNext);
    }
}
