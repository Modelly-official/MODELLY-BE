package modelly.modelly_be.domain.reservation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.reservation.dto.common.DesignerReservationRow;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.domain.reservation.dto.common.ModelReservationRow;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

import static modelly.modelly_be.domain.reservation.entity.QReservation.reservation;
import static modelly.modelly_be.domain.recruitment.entity.QRecruitment.recruitment;
import static modelly.modelly_be.domain.user.entity.QDesigner.designer;
import static modelly.modelly_be.domain.user.entity.QModel.model;
import static modelly.modelly_be.domain.review.entity.QReview.review;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /* ---------- 모델 예약 조회 쿼리 ---------- */

    private BooleanBuilder modelBaseWhere(
            Long modelId,
            YearMonth ym,
            ReservationListType type,
            Category category
    ) {
        // 월 범위 분리 (start: 해당월 1일, end: 다음달 1일)
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);

        // 현재 시간
        LocalDate today = LocalDate.now(KST);
        LocalTime now = LocalTime.now(KST);

        // 조건문 편하게 하고자 사용
        BooleanBuilder where = new BooleanBuilder();

        // 조건1: 해당 모델 예약 + 해당 달
        where.and(reservation.model.id.eq(modelId));
        where.and(reservation.date.goe(start));
        where.and(reservation.date.lt(end));

        // 조건2: CONFIRMED된 예약만
        where.and(reservation.status.eq(ReservationStatus.RESERVATION_CONFIRMED));

        // 조건3: 특정 카테고리만
        if (category != null) {
            where.and(reservation.category.eq(category));
        }

        // 조건4: 다가오는 일정/완료된 일정 분리
        if (type == ReservationListType.UPCOMING) {
            where.and(
                    reservation.date.gt(today)
                            .or(reservation.date.eq(today).and(reservation.endTime.goe(now)))
            );// 예약 date > today || ((date==today) && endtime >= now): 현재 시간보다 endtime이 미래면 다가오는 일정
        } else {
            where.and(
                    reservation.date.lt(today)
                            .or(reservation.date.eq(today).and(reservation.endTime.lt(now)))
            );// 예약 date < today || ((date==today) && endtime < now): 현재 시간보다 endtime이 과거면 완료된 일정
        }

        return where;
    }

    @Override
    public List<ModelReservationRow> findModelReservations(
            Long modelId,
            YearMonth ym,
            ReservationListType type,
            Category category,
            LocalDate cursorDate,
            LocalTime cursorTime,
            Long cursorId,
            int sizePlusOne
    ) {
        BooleanBuilder where = modelBaseWhere(modelId, ym, type, category);
        // 조건5: keyset 커서 (date, startTime, id) - date, startTime, id 순으로 비교
        if (cursorId != null && cursorDate != null && cursorTime != null) {
            where.and(
                    reservation.date.gt(cursorDate)
                            .or(reservation.date.eq(cursorDate).and(reservation.startTime.gt(cursorTime)))
                            .or(reservation.date.eq(cursorDate)
                                    .and(reservation.startTime.eq(cursorTime))
                                    .and(reservation.id.gt(cursorId)))
            );
        }

        return queryFactory
                .select(Projections.constructor(
                        ModelReservationRow.class,
                        reservation.id,
                        reservation.date,
                        reservation.startTime,
                        reservation.endTime,
                        reservation.status,
                        reservation.category,

                        recruitment.id,
                        recruitment.title,

                        designer.user.id,
                        designer.id,
                        designer.nickname,
                        designer.shop
                ))
                .from(reservation)
                .leftJoin(reservation.recruitment, recruitment)
                .join(reservation.designer, designer)
                .where(where)
                .orderBy(reservation.date.asc(), reservation.startTime.asc(), reservation.id.asc())
                .limit(sizePlusOne)
                .fetch();
    }

    // 모델 예약 count
    @Override
    public long countModelReservations(Long modelId, YearMonth ym, ReservationListType type, Category category) {
        BooleanBuilder where = modelBaseWhere(modelId, ym, type, category);

        Long cnt = queryFactory
                .select(reservation.id.count())
                .from(reservation)
                .where(where)
                .fetchOne();

        return cnt == null ? 0L : cnt;
    }

    /* ---------- 모델 완료 + 리뷰 미작성 예약 조회 쿼리 ---------- */

    // 공통 where: 모델 + 월 + CONFIRMED + (완료 endTime 기준) + (카테고리 optional) + (리뷰 미작성)
    private BooleanBuilder baseWhereForModelCompletedUnreviewed(
            Long modelId,
            YearMonth ym,
            Category category
    ) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);

        LocalDate today = LocalDate.now(KST);
        LocalTime now = LocalTime.now(KST);

        BooleanBuilder where = new BooleanBuilder();

        // 모델 + 월범위
        where.and(reservation.model.id.eq(modelId));
        where.and(reservation.date.goe(start));
        where.and(reservation.date.lt(end));

        // CONFIRMED만
        where.and(reservation.status.eq(ReservationStatus.RESERVATION_CONFIRMED));

        // 완료 판정: endTime 기준
        where.and(
                reservation.date.lt(today)
                        .or(reservation.date.eq(today).and(reservation.endTime.lt(now)))
        );

        // 카테고리 필터 (전체면 null)
        if (category != null) {
            where.and(reservation.category.eq(category));
        }

        // 리뷰 미작성: left join review 후 review.id is null
        where.and(review.id.isNull());

        return where;
    }

    // 공통 커서 (date, startTime, id) - 정렬키랑 동일
    private BooleanBuilder keysetAfter(
            LocalDate cursorDate,
            LocalTime cursorTime,
            Long cursorId
    ) {
        BooleanBuilder b = new BooleanBuilder();
        if (cursorDate != null && cursorTime != null && cursorId != null) {
            b.and(
                    reservation.date.gt(cursorDate)
                            .or(reservation.date.eq(cursorDate).and(reservation.startTime.gt(cursorTime)))
                            .or(reservation.date.eq(cursorDate)
                                    .and(reservation.startTime.eq(cursorTime))
                                    .and(reservation.id.gt(cursorId)))
            );
        }
        return b;
    }

    @Override
    public List<ModelReservationRow> findModelCompletedUnreviewedReservations(
            Long modelId,
            YearMonth ym,
            Category category,
            LocalDate cursorDate,
            LocalTime cursorTime,
            Long cursorId,
            int sizePlusOne
    ) {
        BooleanBuilder where = baseWhereForModelCompletedUnreviewed(modelId, ym, category);
        where.and(keysetAfter(cursorDate, cursorTime, cursorId));

        return queryFactory
                .select(Projections.constructor(
                        ModelReservationRow.class,
                        reservation.id,
                        reservation.date,
                        reservation.startTime,
                        reservation.endTime,
                        reservation.status,
                        reservation.category,

                        recruitment.id,
                        recruitment.title,

                        designer.user.id,
                        designer.id,
                        designer.nickname,
                        designer.shop
                ))
                .from(reservation)
                .leftJoin(review).on(
                        review.reservation.eq(reservation)
                                .and(review.model.id.eq(reservation.model.id))
                )
                .leftJoin(reservation.recruitment, recruitment)
                .join(reservation.designer, designer)
                .where(where)
                .orderBy(reservation.date.asc(), reservation.startTime.asc(), reservation.id.asc())
                .limit(sizePlusOne)
                .fetch();
    }

    @Override
    public long countModelCompletedUnreviewedReservations(
            Long modelId,
            YearMonth ym,
            Category category
    ) {
        BooleanBuilder where = baseWhereForModelCompletedUnreviewed(modelId, ym, category);

        Long cnt = queryFactory
                .select(reservation.id.count())
                .from(reservation)
                .leftJoin(review).on(
                        review.reservation.eq(reservation)
                                .and(review.model.id.eq(reservation.model.id))
                )
                .where(where)
                .fetchOne();

        return cnt == null ? 0L : cnt;
    }

    /* ---------- 디자이너 예약 조회 쿼리(모델과 유사 but 카테고리 필터링 제거) ---------- */
    private BooleanBuilder designerBaseWhere(Long designerId, YearMonth ym, ReservationListType type) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);

        LocalDate today = LocalDate.now(KST);
        LocalTime now = LocalTime.now(KST);

        BooleanBuilder where = new BooleanBuilder();

        //  디자이너 + 월 범위
        where.and(reservation.designer.id.eq(designerId));
        where.and(reservation.date.goe(start));
        where.and(reservation.date.lt(end));

        // CONFIRMED만
        where.and(reservation.status.eq(ReservationStatus.RESERVATION_CONFIRMED));

        if (type == ReservationListType.UPCOMING) {
            where.and(
                    reservation.date.gt(today)
                            .or(reservation.date.eq(today).and(reservation.endTime.goe(now)))
            );
        } else { // COMPLETED
            where.and(
                    reservation.date.lt(today)
                            .or(reservation.date.eq(today).and(reservation.endTime.lt(now)))
            );
        }

        return where;
    }

    private void applyCursor(BooleanBuilder where, LocalDate cursorDate, LocalTime cursorTime, Long cursorId) {
        if (cursorId == null || cursorDate == null || cursorTime == null) return;

        where.and(
                reservation.date.gt(cursorDate)
                        .or(reservation.date.eq(cursorDate).and(reservation.startTime.gt(cursorTime)))
                        .or(reservation.date.eq(cursorDate)
                                .and(reservation.startTime.eq(cursorTime))
                                .and(reservation.id.gt(cursorId)))
        );
    }

    @Override
    public List<DesignerReservationRow> findDesignerReservations(
            Long designerId,
            YearMonth ym,
            ReservationListType type,
            LocalDate cursorDate,
            LocalTime cursorTime,
            Long cursorId,
            int sizePlusOne
    ) {
        BooleanBuilder where = designerBaseWhere(designerId, ym, type);
        applyCursor(where, cursorDate, cursorTime, cursorId);

        return queryFactory
                .select(Projections.constructor(
                        DesignerReservationRow.class,
                        reservation.id,
                        reservation.date,
                        reservation.startTime,
                        reservation.endTime,
                        reservation.status,

                        recruitment.id,
                        recruitment.title,

                        model.user.id,
                        model.id,
                        model.user.name
                ))
                .from(reservation)
                .leftJoin(reservation.recruitment, recruitment) // recruitment nullable
                .join(reservation.model, model)
                .join(reservation.designer, designer)
                .where(where)
                .orderBy(reservation.date.asc(), reservation.startTime.asc(), reservation.id.asc())
                .limit(sizePlusOne)
                .fetch();
    }

    @Override
    public long countDesignerReservations(Long designerId, YearMonth ym, ReservationListType type) {
        BooleanBuilder where = designerBaseWhere(designerId, ym, type);

        Long cnt = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(where)
                .fetchOne();

        return cnt == null ? 0L : cnt;
    }

    // 한 예약에 딸린 SubCategory 조회 후 list로
    @Override
    public List<ReservationSubCategoryRow> findSubCategoriesByReservationIds(List<Long> reservationIds) {
        if (reservationIds == null || reservationIds.isEmpty()) return List.of();

        EnumPath<SubCategory> sub = Expressions.enumPath(SubCategory.class, "sub");

        return queryFactory
                .select(Projections.constructor(
                        ReservationSubCategoryRow.class,
                        reservation.id,
                        sub
                ))
                .from(reservation)
                .join(reservation.subCategories, sub)
                .where(reservation.id.in(reservationIds))
                .fetch();
    }


}
