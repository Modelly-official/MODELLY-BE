package modelly.modelly_be.domain.calendar.repository;

import modelly.modelly_be.domain.calendar.dto.internal.DesignerCalendarReservationRow;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

public interface DesignerCalendarQueryRepository {

    long countReservations(
            Long designerId,
            YearMonth yearMonth,
            LocalDate date,
            List<ReservationStatus> statuses
    );

    List<DesignerCalendarReservationRow> findReservations(
            Long designerId,
            YearMonth yearMonth,
            LocalDate date,
            List<ReservationStatus> statuses
    );

    //reservationIds에 딸린 subCategory enum들을 한 번에 조회
    List<ReservationSubCategoryRow> findSubCategoriesByReservationIds(List<Long> reservationIds);

    record ReservationSubCategoryRow(Long reservationId, String subCategoryDescription) {}

    // 해당 월에 예약이 존재하는 날짜 목록 조회
    List<LocalDate> findReservedDatesInMonth(
            Long designerId,
            YearMonth yearMonth,
            List<ReservationStatus> statuses
    );
}

