package modelly.modelly_be.domain.like.repository.recruitmentLikeRepository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.dto.response.LikeRecruitmentListResponseDto;
import modelly.modelly_be.domain.like.entity.QRecruitmentLike;
import modelly.modelly_be.domain.recruitment.entity.QRecruitment;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

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

        return queryFactory.select(Projections.constructor(
                LikeRecruitmentListResponseDto.class,
                qRecruitmentLike.id,
                qRecruitment.id,
                qRecruitment.thumbnail))
                .from(qRecruitmentLike)
                .join(qRecruitmentLike.recruitment, qRecruitment)
                .where(booleanBuilder)
                .orderBy(qRecruitmentLike.id.desc())
                .limit(size+1)
                .fetch();
    }
}
