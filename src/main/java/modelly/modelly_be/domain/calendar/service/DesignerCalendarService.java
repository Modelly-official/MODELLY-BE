package modelly.modelly_be.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.calendar.dto.internal.DesignerCalendarReservationRow;
import modelly.modelly_be.domain.calendar.dto.internal.CalendarReservationDotItem;
import modelly.modelly_be.domain.calendar.dto.response.CalendarReservationDotsResponse;
import modelly.modelly_be.domain.calendar.dto.internal.CalendarReservationItem;
import modelly.modelly_be.domain.calendar.dto.response.CalendarReservationResponse;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignerCalendarService {

    private final DesignerService designerService;
    private final ReservationService reservationService; // month 파싱 재사용
    private final DesignerCalendarQueryRepository calendarQueryRepository;

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    // 캘린더에서의 예약 조회(한달/특정 날짜) - 무한스크롤
    @Transactional(readOnly = true)
    public CalendarReservationResponse getCalendarReservations(
            User me,
            String month,
            LocalDate date
    ) {
        Designer designer = designerService.getByUser(me);

        YearMonth ym = parseYearMonthRequired(month);

        if (date != null && !YearMonth.from(date).equals(ym)) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        List<ReservationStatus> statuses = List.of(ReservationStatus.RESERVATION_CONFIRMED);

        long totalCount = calendarQueryRepository.countReservations(designer.getId(), ym, date, statuses);

        List<DesignerCalendarReservationRow> rows = calendarQueryRepository.findReservations(
                designer.getId(),
                ym,
                date,
                statuses
        );

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

        return new CalendarReservationResponse(
                items,
                totalCount
        );
    }


    // 예약이 있는날 DOT 찍는 용도
    @Transactional(readOnly = true)
    public CalendarReservationDotsResponse getReservationDots(User user, String month, boolean includePending) {
        // includePending=false면 CONFIRMED만, true면 CONFIRMED+PENDING 점 표시

        Designer designer = designerService.getByUser(user);

        YearMonth ym = parseYearMonthRequired(month);

        // includePending 여부에 따라 status 설정
        List<ReservationStatus> statuses = includePending
                ? List.copyOf(EnumSet.of(ReservationStatus.RESERVATION_CONFIRMED, ReservationStatus.RESERVATION_PENDING))
                : List.of(ReservationStatus.RESERVATION_CONFIRMED);

        List<LocalDate> reservedDates = calendarQueryRepository.findReservedDatesInMonth(designer.getId(), ym, statuses);
        Set<LocalDate> reservedSet = new HashSet<>(reservedDates);

        int lastDay = ym.lengthOfMonth();
        List<CalendarReservationDotItem> days = java.util.stream.IntStream.rangeClosed(1, lastDay)
                .mapToObj(d -> {
                    LocalDate date = ym.atDay(d);
                    return new CalendarReservationDotItem(date, reservedSet.contains(date));
                })
                .toList();

        return new CalendarReservationDotsResponse(ym.toString(), days);
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

            return enumName;
        }
    }
}
