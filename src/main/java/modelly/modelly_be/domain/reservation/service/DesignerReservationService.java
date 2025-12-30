package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.reservation.dto.internal.DesignerReservationRow;
import modelly.modelly_be.domain.reservation.dto.response.*;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.domain.reservation.repository.ReservationQueryRepository;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignerReservationService {

    private final DesignerService designerService;
    private final ReservationQueryRepository reservationQueryRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private final ReservationService reservationService;

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
}
