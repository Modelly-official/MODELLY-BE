package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCreateRequest;
import modelly.modelly_be.domain.reservation.dto.response.ModelReservationItem;
import modelly.modelly_be.domain.reservation.dto.common.ModelReservationRow;
import modelly.modelly_be.domain.reservation.dto.response.ReservationScrollResponse;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.repository.ReservationQueryRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.Category;
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
public class ModelReservationService {

    private final ReservationService reservationService;
    private final ModelService modelService;
    private final RecruitmentService recruitmentService;
    private final ReservationQueryRepository reservationQueryRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");


    // 예약 신청
    @Transactional
    public void createReservation(User user, ReservationCreateRequest req) {
        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        Recruitment recruitment = recruitmentService.getById(req.recruitmentId());
        Designer designer = recruitment.getDesigner();

        // HH:mm 형식으로 포맷팅 후 parsing
        DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate date = req.date();
        LocalTime start = LocalTime.parse(req.startTime(), HM);
        LocalTime end = start.plusMinutes(30);

        // 디자이너 단위로 예약 충돌 방지
        if (reservationService.existsTimeConflict(designer, date, start, end)) {
            throw new GeneralException(ErrorStatus.RESERVATION_TIME_CONFLICT);
        }

        // 디자이너의 동일 slot(시간대) Lock + 예약 처리
        List<RecruitmentTime> slots =
                recruitmentService.getAllRecruitmentTimesForUpdate(designer.getId(), date, start);

        if (slots.stream().anyMatch(RecruitmentTime::isReserved)) {
            throw new GeneralException(ErrorStatus.RESERVATION_TIME_CONFLICT);
        }
        slots.forEach(RecruitmentTime::reserve);

        Reservation reservation = Reservation.builder()
                .date(date)
                .startTime(start)
                .endTime(end)
                .category(req.category())
                .subCategories(req.subCategories())
                .comment(req.comment())
                .designerName(req.designerName())
                .shop(req.shop())
                .status(ReservationStatus.RESERVATION_PENDING)
                .imageUrl(req.imageUrls())
                .recruitment(recruitment)
                .model(model)
                .designer(recruitment.getDesigner())
                .build();

        reservationService.save(reservation);
    }

    // 예약 조회(다가오는 일정, 완료된 일정)
    @Transactional(readOnly = true)
    public ReservationScrollResponse<ModelReservationItem> getModelReservations(
            User user,
            String month, // yyyy-MM
            ReservationListType type,
            Category category,
            int size,
            LocalDate cursorDate,
            String cursorTime, // HH:mm
            Long cursorId
    ) {
        // 모델 권한 체크
        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        // month 파싱 (아래 유틸 참고)
        YearMonth ym = parseYearMonthOrNow(month);

        // cursorTime(String)을 LocalTime으로 변환
        LocalTime cursorTimeParsed = (cursorTime == null || cursorTime.isBlank())
                ? null
                : LocalTime.parse(cursorTime, HM);

        // totalCount 먼저 계산
        long totalCount = reservationQueryRepository.countModelReservations(
                model.getId(),
                ym,
                type,
                category
        );

        // 모델 예약 정보 가져오기
        List<ModelReservationRow> rows = reservationQueryRepository.findModelReservations(
                model.getId(),
                ym,
                type,
                category,
                cursorDate,
                cursorTimeParsed,
                cursorId,
                size + 1 // size + 1개 호출
        );

        // hasNext 여부 파악 후 size만큼 자르기
        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        // 이번 페이지에 포함된 id 목록
        List<Long> ids = rows.stream().map(ModelReservationRow::reservationId).toList();

        // subCategories를 한 번에 땡겨서 Map으로 묶기
        Map<Long, List<SubCategory>> subMap =
                reservationQueryRepository.findSubCategoriesByReservationIds(ids).stream()
                        .collect(Collectors.groupingBy(
                                ReservationQueryRepository.ReservationSubCategoryRow::reservationId,
                                Collectors.mapping(ReservationQueryRepository.ReservationSubCategoryRow::subCategory, Collectors.toList())
                        ));

        // 최종 응답 반환
        List<ModelReservationItem> items = rows.stream()
                .map(r -> new ModelReservationItem(
                        r.reservationId(),
                        r.recruitmentId(),
                        r.recruitmentTitle(),
                        r.designerUserId(),
                        r.designerId(),
                        r.designerNickname(),
                        r.shop(),
                        r.category(),
                        subMap.getOrDefault(r.reservationId(), List.of()),
                        r.date(),
                        r.startTime().format(HM),
                        r.endTime().format(HM),
                        r.status()
                ))
                .toList();

        // 다음 커서 계산
        LocalDate nextDate = null;
        String nextTime = null;
        Long nextId = null;

        if (hasNext && !items.isEmpty()) {
            ModelReservationItem last = items.get(items.size() - 1);
            nextDate = last.date();
            nextTime = last.startTime();
            nextId = last.reservationId();
        }

        return new ReservationScrollResponse<>(items, totalCount, hasNext, nextDate, nextTime, nextId);
    }


    // month 파싱
    private YearMonth parseYearMonthOrNow(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now(KST);
        }
        try {
            return YearMonth.parse(month);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.MONTH_BAD_REQUEST);
        }
    }
}
