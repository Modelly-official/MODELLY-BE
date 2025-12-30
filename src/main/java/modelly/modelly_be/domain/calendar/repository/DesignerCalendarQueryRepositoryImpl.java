package modelly.modelly_be.domain.calendar.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.calendar.dto.internal.DesignerCalendarReservationRow;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.global.entity.SubCategory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static modelly.modelly_be.domain.reservation.entity.QReservation.reservation;
import static modelly.modelly_be.domain.recruitment.entity.QRecruitment.recruitment;
import static modelly.modelly_be.domain.user.entity.QDesigner.designer;
import static modelly.modelly_be.domain.user.entity.QModel.model;

@Repository
@RequiredArgsConstructor
public class DesignerCalendarQueryRepositoryImpl implements DesignerCalendarQueryRepository {

    private final JPAQueryFactory queryFactory;

    private BooleanBuilder baseWhere(Long designerId, YearMonth ym, LocalDate date, List<ReservationStatus> statuses) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);

        BooleanBuilder where = new BooleanBuilder();
        where.and(reservation.designer.id.eq(designerId));

        if (statuses != null && !statuses.isEmpty()) {
            where.and(reservation.status.in(statuses));
        }

        // month 범위 항상 적용
        where.and(reservation.date.goe(start));
        where.and(reservation.date.lt(end));

        // date가 오면 그 날만
        if (date != null) {
            where.and(reservation.date.eq(date));
        }

        return where;
    }

    private void applyCursor(BooleanBuilder where, LocalDate cursorDate, LocalTime cursorTime, Long cursorId) {
        if (cursorDate == null || cursorTime == null || cursorId == null) return;

        // 정렬키
        where.and(
                reservation.date.gt(cursorDate)
                        .or(reservation.date.eq(cursorDate).and(reservation.startTime.gt(cursorTime)))
                        .or(reservation.date.eq(cursorDate)
                                .and(reservation.startTime.eq(cursorTime))
                                .and(reservation.id.gt(cursorId)))
        );
    }

    @Override
    public long countReservations(Long designerId, YearMonth yearMonth, LocalDate date, List<ReservationStatus> statuses) {
        BooleanBuilder where = baseWhere(designerId, yearMonth, date, statuses);

        Long cnt = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(where)
                .fetchOne();

        return cnt == null ? 0L : cnt;
    }

    @Override
    public List<DesignerCalendarReservationRow> findReservations(
            Long designerId,
            YearMonth yearMonth,
            LocalDate date,
            List<ReservationStatus> statuses,
            LocalDate cursorDate,
            LocalTime cursorTime,
            Long cursorId,
            int sizePlusOne
    ) {
        BooleanBuilder where = baseWhere(designerId, yearMonth, date, statuses);
        applyCursor(where, cursorDate, cursorTime, cursorId);

        return queryFactory
                .select(Projections.constructor(
                        DesignerCalendarReservationRow.class,
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
                .leftJoin(reservation.recruitment, recruitment)
                .join(reservation.model, model)
                .join(reservation.designer, designer)
                .where(where)
                .orderBy(reservation.date.asc(), reservation.startTime.asc(), reservation.id.asc())
                .limit(sizePlusOne)
                .fetch();
    }

    @Override
    public List<ReservationSubCategoryRow> findSubCategoriesByReservationIds(List<Long> reservationIds) {
        if (reservationIds == null || reservationIds.isEmpty()) return List.of();

        EnumPath<SubCategory> sub = Expressions.enumPath(SubCategory.class, "sub");

        return queryFactory
                .select(Projections.constructor(
                        ReservationSubCategoryRow.class,
                        reservation.id,
                        sub.stringValue() // enum name
                ))
                .from(reservation)
                .join(reservation.subCategories, sub)
                .where(reservation.id.in(reservationIds))
                .fetch()
                .stream()
                .toList();
    }
}
