package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.service.ChatRoomService;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.reservation.dto.response.ChatRoomReservationSummary;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.repository.ReservationRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ChatRoomService chatRoomService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public void hasPendingOrConfirmedReservation(Recruitment recruitment) {
        List<Reservation> reservations = reservationRepository.findAllByRecruitment(recruitment);

        LocalDateTime now = LocalDateTime.now();

        boolean hasPending = reservations.stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.RESERVATION_PENDING);

        if (hasPending) {
            throw new GeneralException(ErrorStatus.CAN_NOT_RECRUITMENT_DELETE_OR_MODIFY);
        }

        boolean hasFutureConfirmed = reservations.stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.RESERVATION_CONFIRMED
                        && LocalDateTime.of(r.getDate(), r.getStartTime()).isAfter(now));

        if (hasFutureConfirmed) {
            throw new GeneralException(ErrorStatus.CAN_NOT_RECRUITMENT_DELETE_OR_MODIFY);
        }
    }

    @Transactional
    public void deleteRelationshipWithRecruitment(Recruitment recruitment) {
        List<Reservation> reservations = reservationRepository.findAllByRecruitment(recruitment);

        for (Reservation r : reservations) {
            r.deleteRelationShip();
            reservationRepository.save(r);
        }
    }

    public Reservation getById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION));
    }

    // 예약 저장
    @Transactional
    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public boolean existsTimeConflict(Designer designer, LocalDate date, LocalTime start, LocalTime end) {
        return reservationRepository.existsByDesignerAndDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                designer,
                date,
                List.of(ReservationStatus.RESERVATION_CONFIRMED, ReservationStatus.RESERVATION_PENDING),
                end,
                start
        );
    }

    @Transactional(readOnly = true)
    public ChatRoomReservationSummary getChatRoomReservationSummary(Long roomId, User me) {
        ChatRoom room = chatRoomService.getById(roomId);

        if (!room.isParticipant(me)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        Long modelId = room.getModel().getId();
        Long designerId = room.getDesigner().getId();

        LocalDate today = LocalDate.now(KST);
        LocalTime now = LocalTime.now(KST);

        // CONFIRMED + 다가오는 예약 1개
        Reservation r = reservationRepository
                .findUpcomingConfirmedForChatRoom(
                        modelId,
                        designerId,
                        today,
                        now,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElse(null);

        if (r == null) return null;

        // 상대방 정보 결정 단계
        boolean meIsModel = room.getModel().getUser().getId().equals(me.getId());

        Long opponentUserId = meIsModel
                ? room.getDesigner().getUser().getId()
                : room.getModel().getUser().getId();

        String opponentName = meIsModel
                ? room.getDesigner().getNickname()          // 모델 입장: 디자이너 닉네임
                : room.getModel().getUser().getName();      // 디자이너 입장: 모델 실명

        Recruitment recruitment = r.getRecruitment(); // nullable 가능
        Long recruitmentId = (recruitment != null) ? recruitment.getId() : null;
        String recruitmentTitle = (recruitment != null) ? recruitment.getTitle() : null;

        return new ChatRoomReservationSummary(
                recruitmentId,
                recruitmentTitle,
                r.getDate(),
                r.getStartTime().format(HM),
                r.getEndTime().format(HM),
                opponentUserId,
                opponentName
        );
    }
}
