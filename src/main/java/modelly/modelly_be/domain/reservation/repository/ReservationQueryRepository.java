package modelly.modelly_be.domain.reservation.repository;

import modelly.modelly_be.domain.reservation.dto.common.DesignerReservationRow;
import modelly.modelly_be.domain.reservation.dto.common.ModelReservationRow;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

public interface ReservationQueryRepository {
    // 모델 예약 조회
    List<ModelReservationRow> findModelReservations(
            Long modelId,
            YearMonth yearMonth,
            ReservationListType type,
            Category category,
            LocalDate cursorDate,
            LocalTime cursorTime,
            Long cursorId,
            int sizePlusOne
    );

    // 모델 totalCount
    long countModelReservations(
            Long modelId,
            YearMonth yearMonth,
            ReservationListType type,
            Category category
    );


    // 디자이너 예약 조회
    List<DesignerReservationRow> findDesignerReservations(
            Long designerId,
            YearMonth yearMonth,
            ReservationListType type,
            LocalDate cursorDate,
            LocalTime cursorTime,
            Long cursorId,
            int sizePlusOne
    );

    // 디자이너 totalCount
    long countDesignerReservations(
            Long designerId,
            YearMonth yearMonth,
            ReservationListType type
    );

    // subCategories를 한 번에 가져오기
    List<ReservationSubCategoryRow> findSubCategoriesByReservationIds(List<Long> reservationIds);

    record ReservationSubCategoryRow(Long reservationId, SubCategory subCategory) {}


}
