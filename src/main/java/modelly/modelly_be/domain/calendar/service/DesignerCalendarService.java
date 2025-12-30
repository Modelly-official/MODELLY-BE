package modelly.modelly_be.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.calendar.dto.internal.DesignerCalendarReservationRow;
import modelly.modelly_be.domain.calendar.dto.response.CalendarReservationItem;
import modelly.modelly_be.domain.calendar.dto.response.CalendarReservationScrollResponse;
import modelly.modelly_be.domain.calendar.repository.DesignerCalendarQueryRepository;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.SubCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignerCalendarService {

    private final DesignerService designerService;
    private final ReservationService reservationService; // month 파싱 재사용
    private final DesignerCalendarQueryRepository calendarQueryRepository;

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    @Transactional(readOnly = true)
    public CalendarReservationScrollResponse getCalendarReservations(
            User me,
            String month,
            LocalDate date,
            int size,
            LocalDate cursorDate,
            String cursorTime,
            Long cursorId
    ) {
        Designer designer = designerService.getByUser(me);

        YearMonth ym = parseYearMonthRequired(month);

        if (date != null && !YearMonth.from(date).equals(ym)) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        LocalTime cursorTimeParsed = (cursorTime == null || cursorTime.isBlank())
                ? null
                : parseCursorTime(cursorTime);

        List<ReservationStatus> statuses = List.of(ReservationStatus.RESERVATION_CONFIRMED);

        long totalCount = calendarQueryRepository.countReservations(designer.getId(), ym, date, statuses);

        List<DesignerCalendarReservationRow> rows = calendarQueryRepository.findReservations(
                designer.getId(),
                ym,
                date,
                statuses,
                cursorDate,
                cursorTimeParsed,
                cursorId,
                size + 1
        );

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        List<Long> ids = rows.stream().map(DesignerCalendarReservationRow::reservationId).toList();

        // subcategory 한 번에
        Map<Long, List<String>> subMap =
                calendarQueryRepository.findSubCategoriesByReservationIds(ids).stream()
                        .collect(Collectors.groupingBy(
                                DesignerCalendarQueryRepository.ReservationSubCategoryRow::reservationId,
                                Collectors.mapping(r -> enumNameToDescription(r.subCategoryDescription()), Collectors.toList())
                        ));

        List<CalendarReservationItem> items = rows.stream()
                .map(r -> new CalendarReservationItem(
                        r.reservationId(),
                        r.recruitmentId(),
                        r.modelUserId(),
                        r.modelId(),
                        r.modelName(),
                        subMap.getOrDefault(r.reservationId(), List.of()),
                        r.date(),
                        r.startTime().format(HM),
                        r.endTime().format(HM)
                ))
                .toList();

        LocalDate nextCursorDate = null;
        String nextCursorTime = null;
        Long nextCursorId = null;

        if (hasNext && !items.isEmpty()) {
            CalendarReservationItem last = items.get(items.size() - 1);
            nextCursorDate = last.date();
            nextCursorTime = last.startTime();
            nextCursorId = last.reservationId();
        }

        return new CalendarReservationScrollResponse(
                items,
                totalCount,
                hasNext,
                nextCursorDate,
                nextCursorTime,
                nextCursorId
        );
    }

    private YearMonth parseYearMonthRequired(String month) {
        if (month == null || month.isBlank()) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }
        try {
            return reservationService.parseYearMonthOrNow(month);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }
    }

    private LocalTime parseCursorTime(String cursorTime) {
        try {
            return LocalTime.parse(cursorTime, HM);
        } catch (DateTimeParseException e) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }
    }

    private String enumNameToDescription(String enumName) {
        try {
            return SubCategory.valueOf(enumName).getDescription();
        } catch (Exception e) {
            // enumName이 이상하면 그냥 원문 노출 (혹은 빈 문자열 처리)
            return enumName;
        }
    }
}
