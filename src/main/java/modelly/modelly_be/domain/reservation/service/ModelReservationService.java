package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.event.dto.reservation.ReservationCreatedEvent;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCreateRequest;
import modelly.modelly_be.domain.reservation.dto.response.AvailableReservationScheduleResponse;
import modelly.modelly_be.domain.reservation.dto.response.ModelReservationItem;
import modelly.modelly_be.domain.reservation.dto.internal.ModelReservationRow;
import modelly.modelly_be.domain.reservation.dto.response.ReservationScrollResponse;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.repository.ReservationQueryRepository;
import modelly.modelly_be.domain.reservation.repository.ReservationRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    /*----------- 예약 신청 ----------*/
    @Transactional
    public void createReservation(User user, ReservationCreateRequest req) {
        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        Recruitment recruitment = recruitmentService.getById(req.recruitmentId());
        Designer designer = recruitment.getDesigner();

        LocalDate date = req.date();
        LocalTime start = LocalTime.parse(req.startTime(), HM);
        LocalTime end = start.plusMinutes(30);

        // 디자이너 단위로 예약 충돌 방지
        // time slot이 없거나 reserve = true면 exception 발생
        if (!recruitmentService.isSlotAvailable(designer, date, start)) {
            throw new GeneralException(ErrorStatus.RESERVATION_TIME_CONFLICT);
        }

        // 중복 신청 방지
        if (reservationService.existsDuplicateApplication(model.getId(), recruitment.getId())) {
            throw new GeneralException(ErrorStatus.RESERVATION_ALREADY_APPLIED);
        }

        String imageUrl = (req.imageUrls() == null || req.imageUrls().isBlank())
                ? null
                : req.imageUrls();

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
                .imageUrl(imageUrl)
                .recruitment(recruitment)
                .model(model)
                .designer(recruitment.getDesigner())
                .build();

        reservationService.save(reservation);

        if (designer.getUser().getNotificationSetting().isReservationNotification()) {
            eventPublisher.publishEvent(new ReservationCreatedEvent(reservation));
        }
    }

    /*----------- 예약 조회(다가오는 일정, 완료된 일정) ----------*/
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

        // month 파싱 (reservationService 유틸 참고)
        YearMonth ym = (month == null || month.isBlank())
                ? null
                : reservationService.parseYearMonthOrNow(month);

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
                        r.category().getDescription(),
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

    /* ---------- 리뷰 미작성 일정 조회 ---------- */
    @Transactional(readOnly = true)
    public ReservationScrollResponse<ModelReservationItem> getModelCompletedUnreviewedReservations(
            User user,
            String month,               // yyyy-MM (없으면 현재달)
            Category category,          // null이면 전체
            int size,
            LocalDate cursorDate,
            String cursorTime,          // HH:mm
            Long cursorId
    ) {
        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        YearMonth ym = reservationService.parseYearMonthOrNow(month);

        LocalTime cursorTimeParsed = (cursorTime == null || cursorTime.isBlank())
                ? null
                : LocalTime.parse(cursorTime, HM);

        // totalCount (월 + 카테고리 + 완료 + 미작성 조건)
        long totalCount = reservationQueryRepository.countModelCompletedUnreviewedReservations(
                model.getId(), ym, category
        );

        List<ModelReservationRow> rows =
                reservationQueryRepository.findModelCompletedUnreviewedReservations(
                        model.getId(),
                        ym,
                        category,
                        cursorDate,
                        cursorTimeParsed,
                        cursorId,
                        size + 1
                );

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        List<Long> ids = rows.stream().map(ModelReservationRow::reservationId).toList();

        Map<Long, List<SubCategory>> subMap =
                reservationQueryRepository.findSubCategoriesByReservationIds(ids).stream()
                        .collect(Collectors.groupingBy(
                                ReservationQueryRepository.ReservationSubCategoryRow::reservationId,
                                Collectors.mapping(
                                        ReservationQueryRepository.ReservationSubCategoryRow::subCategory,
                                        Collectors.toList()
                                )
                        ));

        List<ModelReservationItem> items = rows.stream()
                .map(r -> new ModelReservationItem(
                        r.reservationId(),
                        r.recruitmentId(),
                        r.recruitmentTitle(),
                        r.designerUserId(),
                        r.designerId(),
                        r.designerNickname(),
                        r.shop(),
                        r.category().getDescription(),
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
            ModelReservationItem last = items.get(items.size() - 1);
            nextDate = last.date();
            nextTime = last.startTime();
            nextId = last.reservationId();
        }

        return new ReservationScrollResponse<>(items, totalCount, hasNext, nextDate, nextTime, nextId);
    }

    // 특정 공고의 예약 가능한 스케줄 조회
    @Transactional(readOnly = true)
    public AvailableReservationScheduleResponse getAvailableSchedules(Long recruitmentId, String month){
        return recruitmentService.getAvailableSchedules(recruitmentId, month);
    }

    // 예약 신청 취소
    @Transactional
    public SimpleMessageDTO cancelPendingReservation(User user, Long reservationId) {
        Model model = modelService.getModelByUser(user);

        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION));

        // 내 예약인지 체크
        if (reservation.getModel() == null || !reservation.getModel().getId().equals(model.getId())) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        // 대기중만 취소 가능
        if (reservation.getStatus() != ReservationStatus.RESERVATION_PENDING) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        reservation.cancelByModel();
        return new SimpleMessageDTO("예약 신청이 취소되었습니다.");
    }
}
