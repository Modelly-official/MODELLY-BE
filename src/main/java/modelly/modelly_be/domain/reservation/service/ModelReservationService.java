package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCreateRequest;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelReservationService {

    private final ReservationService reservationService;
    private final ModelService modelService;
    private final RecruitmentService recruitmentService;
    private final DesignerService designerService;

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
}
