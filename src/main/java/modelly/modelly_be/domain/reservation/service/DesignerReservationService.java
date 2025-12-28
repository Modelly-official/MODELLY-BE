package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.reservation.dto.internal.DesignerDailyReservationItem;
import modelly.modelly_be.domain.reservation.dto.internal.DesignerPendingReservationItem;
import modelly.modelly_be.domain.reservation.dto.internal.DesignerReservationRow;
import modelly.modelly_be.domain.reservation.dto.response.*;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.repository.ReservationQueryRepository;
import modelly.modelly_be.domain.reservation.repository.ReservationRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.entity.SubCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignerReservationService {

    private final DesignerService designerService;
    private final ReservationService reservationService;
    private final ReservationQueryRepository reservationQueryRepository;
    private final ReservationRepository reservationRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    @Transactional(readOnly = true)
    public ReservationScrollResponse<DesignerReservationItem> getDesignerReservations(
            User user,
            String month, // yyyy-MM
            ReservationListType type,
            int size,
            LocalDate cursorDate,
            String cursorTime, // HH:mm
            Long cursorId
    ) {
        // 권한/대상
        Designer designer = designerService.getByUser(user);

        YearMonth ym = reservationService.parseYearMonthOrNow(month);

        LocalTime cursorTimeParsed = (cursorTime == null || cursorTime.isBlank())
                ? null
                : LocalTime.parse(cursorTime, HM);

        // totalCount (같은 조건)
        long totalCount = reservationQueryRepository.countDesignerReservations(designer.getId(), ym, type);

        // list
        List<DesignerReservationRow> rows = reservationQueryRepository.findDesignerReservations(
                designer.getId(),
                ym,
                type,
                cursorDate,
                cursorTimeParsed,
                cursorId,
                size + 1
        );

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        List<Long> ids = rows.stream().map(DesignerReservationRow::reservationId).toList();

        // 표시용 subCategories 한번에 땡겨오기
        Map<Long, List<SubCategory>> subMap =
                reservationQueryRepository.findSubCategoriesByReservationIds(ids).stream()
                        .collect(Collectors.groupingBy(
                                ReservationQueryRepository.ReservationSubCategoryRow::reservationId,
                                Collectors.mapping(ReservationQueryRepository.ReservationSubCategoryRow::subCategory, Collectors.toList())
                        ));

        List<DesignerReservationItem> items = rows.stream()
                .map(r -> new DesignerReservationItem(
                        r.reservationId(),
                        r.recruitmentId(),
                        r.recruitmentTitle(),
                        r.modelUserId(),
                        r.modelId(),
                        r.modelName(),
                        subMap.getOrDefault(r.reservationId(), List.of())
                                .stream()
                                .map(SubCategory::getDescription)
                                .toList(),
                        r.date(),
                        r.startTime().format(HM),
                        r.endTime().format(HM),
                        r.status()
                ))
                .toList();

        LocalDate nextDate = null;
        String nextTime = null;
        Long nextId = null;

        if (hasNext && !items.isEmpty()) {
            DesignerReservationItem last = items.get(items.size() - 1);
            nextDate = last.date();
            nextTime = last.startTime();
            nextId = last.reservationId();
        }

        return new ReservationScrollResponse<>(
                items,
                totalCount,
                hasNext,
                nextDate,
                nextTime,
                nextId
        );
    }

    // 오늘의 예약 조회
    @Transactional(readOnly = true)
    public DesignerDailyReservationResponse getDailyReservations(User me, LocalDate date) {
        designerService.checkDesigner(me);

        List<Reservation> reservations =
                reservationRepository.findAllByDesigner_User_IdAndDateAndStatusOrderByStartTimeAsc(
                        me.getId(),
                        date,
                        ReservationStatus.RESERVATION_CONFIRMED
                );

        List<DesignerDailyReservationItem> items = reservations.stream()
                .map(r -> new DesignerDailyReservationItem(
                        r.getId(),
                        r.getStartTime().format(HM),
                        r.getModel().getUser().getName(),
                        extractSubCategoryLabels(r)
                ))
                .toList();

        return DesignerDailyReservationResponse.of(date, items);
    }

    @Transactional(readOnly = true)
    public DesignerPendingReservationScrollResponse getPendingReservations(
            User me,
            int size,
            LocalDate cursorDate,
            String cursorTime,
            Long cursorId
    ) {
        designerService.checkDesigner(me);

        // pending 상태의 예약 총 수 조회
        int totalCount = reservationQueryRepository.countDesignerPending(me.getId(), ReservationStatus.RESERVATION_PENDING);

        LocalTime ct = (cursorDate != null && cursorTime != null && cursorId != null)
                ? LocalTime.parse(cursorTime, HM)
                : null;

        // 신규 예약 조회 (페이지 단위)
        List<Reservation> fetched = reservationQueryRepository.findDesignerPendingAfterCursor(
                me.getId(),
                ReservationStatus.RESERVATION_PENDING,
                cursorDate,
                ct,
                cursorId,
                size + 1
        );

        // hasNext 판단 후 size만큼 자르기
        boolean hasNext = fetched.size() > size;
        List<Reservation> page = hasNext ? fetched.subList(0, size) : fetched;

        List<DesignerPendingReservationItem> items = page.stream()
                .map(r -> new DesignerPendingReservationItem(
                        r.getId(),
                        r.getDate(),
                        r.getStartTime().format(HM),
                        r.getModel().getUser().getName(),
                        extractSubCategoryLabels(r)
                ))
                .toList();

        // Cursor 설정
        LocalDate nextCursorDate = null;
        String nextCursorTime = null;
        Long nextCursorId = null;

        if (!items.isEmpty()) {
            DesignerPendingReservationItem last = items.get(items.size() - 1);
            nextCursorDate = last.date();
            nextCursorTime = last.time();
            nextCursorId = last.reservationId();
        }

        return new DesignerPendingReservationScrollResponse(
                totalCount,
                hasNext,
                items,
                nextCursorDate,
                nextCursorTime,
                nextCursorId
        );
    }

    // 예약의 subcategory 추출
    private List<String> extractSubCategoryLabels(Reservation r) {

        if (r.getSubCategories() == null || r.getSubCategories().isEmpty()) {
            return Collections.emptyList();
        }

        return r.getSubCategories().stream()
                .map(sc -> sc.getDescription())
                .toList();
    }
}
