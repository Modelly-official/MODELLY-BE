package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.response.ReservationChangeChatPayload;
import modelly.modelly_be.domain.chat.dto.response.SendMessageResponse;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;
import modelly.modelly_be.domain.chat.service.ChatRoomService;
import modelly.modelly_be.domain.chat.service.ChattingService;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.reservation.dto.request.ReservationChangeCreateRequest;
import modelly.modelly_be.domain.reservation.dto.response.ChatRoomReservationSummary;
import modelly.modelly_be.domain.reservation.dto.response.ReservationChangeCreateResponse;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.ReservationChange;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationChangeStatus;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.repository.ReservationChangeRepository;
import modelly.modelly_be.domain.reservation.repository.ReservationRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationChangeRepository reservationChangeRepository;

    private final ChatRoomService chatRoomService;
    private final ChattingService chattingService;

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

    // 채팅방과 연결된 예약 내역 조회
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

    // 예약 변경 요청
    @Transactional
    public void createChangeRequest(
            Long reservationId,
            Long roomId, // nullable
            User me,
            ReservationChangeCreateRequest req
    ) {
        // 예약 존재 여부 확인
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION));

        // 요청자가 예약의 참가자인지 확인
        boolean meIsModel = reservation.getModel() != null
                && reservation.getModel().getUser().getId().equals(me.getId());
        boolean meIsDesigner = reservation.getDesigner() != null
                && reservation.getDesigner().getUser().getId().equals(me.getId());

        if (!meIsModel && !meIsDesigner) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        // 확정된 예약만 변경 요청 가능
        if (reservation.getStatus() != ReservationStatus.RESERVATION_CONFIRMED) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        // roomId가 없으면 상대 userId 구해서 채팅방 open(없으면 생성)
        Long finalRoomId = roomId;
        if (finalRoomId == null) {
            Long opponentUserId = meIsModel
                    ? reservation.getDesigner().getUser().getId()
                    : reservation.getModel().getUser().getId();

            finalRoomId = chatRoomService.openRoom(me.getId(), opponentUserId).getChatRoomId();
        }

        ChatRoom room = chatRoomService.getById(finalRoomId);

        // room의 pair가 reservation의 pair랑 맞는지 검증
        if (!room.getModel().getId().equals(reservation.getModel().getId())
                || !room.getDesigner().getId().equals(reservation.getDesigner().getId())) {
            throw new GeneralException(ErrorStatus.INVALID_CHATROOM);
        }

        // PENDING 중복 방지 (예약 1개당 PENDING 1개)
        if (reservationChangeRepository.existsByReservation_IdAndStatus(reservationId, ReservationChangeStatus.PENDING)) {
            throw new GeneralException(ErrorStatus.RESERVATION_CHANGE_ALREADY_PENDING);
        }

        // proposed time 파싱 + endTime 계산
        LocalTime proposedStart = LocalTime.parse(req.proposedStartTime(), HM);
        long minutes = Duration.between(reservation.getStartTime(), reservation.getEndTime()).toMinutes();
        LocalTime proposedEnd = proposedStart.plusMinutes(minutes);

        // 디자이너 스케줄 conflict 체크
        boolean conflict = reservationRepository.existsConflictOnDesignerSchedule(
                reservation.getDesigner().getId(),
                req.proposedDate(),
                proposedStart,
                proposedEnd,
                ReservationStatus.RESERVATION_CONFIRMED,
                reservation.getId()
        );
        if (conflict) {
            throw new GeneralException(ErrorStatus.RESERVATION_TIME_CONFLICT);
        }

        // 저장
        ReservationChange change = ReservationChange.builder()
                .reservation(reservation)
                .chatRoom(room)
                .requesterUserId(me.getId())
                .proposedDate(req.proposedDate())
                .proposedStartTime(proposedStart)
                .proposedEndTime(proposedEnd)
                .reason(req.reason())
                .status(ReservationChangeStatus.PENDING)
                .build();

        ReservationChange saved = reservationChangeRepository.save(change);

        // 채팅 payload 만들기
        ReservationChangeChatPayload payload = new ReservationChangeChatPayload(
                "CHANGE_REQUEST",
                saved.getId(),
                reservationId,
                reservation.getDate(),
                reservation.getStartTime().format(HM),
                reservation.getEndTime().format(HM),
                saved.getProposedDate(),
                saved.getProposedStartTime().format(HM),
                saved.getProposedEndTime().format(HM),
                saved.getReason()
        );

        // 채팅 저장 + STOMP 브로드캐스트
        chattingService.publishReservationPayload(me.getId(), finalRoomId, payload);
    }

    // 예약 변경 요청 수락
    @Transactional
    public SimpleMessageDTO acceptReservationChange(Long changeId, User me) {
        // 변경요청 조회
        ReservationChange change = reservationChangeRepository.findById(changeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION_CHANGE));

        // pending 상태인지 확인
        if (!change.isPending()) {
            throw new GeneralException(ErrorStatus.RESERVATION_CHANGE_NOT_PENDING);
        }

        Reservation reservation = change.getReservation();

        // 참여자 검증
        boolean meIsModel = reservation.getModel() != null
                && reservation.getModel().getUser().getId().equals(me.getId());
        boolean meIsDesigner = reservation.getDesigner() != null
                && reservation.getDesigner().getUser().getId().equals(me.getId());

        if (!meIsModel && !meIsDesigner) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        // 요청자가 수락/거절하는 거 방지
        if (change.getRequesterUserId().equals(me.getId())) {
            throw new GeneralException(ErrorStatus.RESERVATION_CHANGE_SELF_RESPONSE_NOT_ALLOWED);
        }

        // 확정 예약만 반영
        if (reservation.getStatus() != ReservationStatus.RESERVATION_CONFIRMED) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        // 수락 시점에 conflict 재검증
        boolean conflict = reservationRepository.existsConflictOnDesignerSchedule(
                reservation.getDesigner().getId(),
                change.getProposedDate(),
                change.getProposedStartTime(),
                change.getProposedEndTime(),
                ReservationStatus.RESERVATION_CONFIRMED,
                reservation.getId() // 자기 예약 제외
        );

        if (conflict) {
            throw new GeneralException(ErrorStatus.RESERVATION_TIME_CONFLICT);
        }

        // 예약에 반영
        reservation.applySchedule(
                change.getProposedDate(),
                change.getProposedStartTime(),
                change.getProposedEndTime()
        );

        // 변경요청 ACCEPTED 처리
        change.accept(me.getId());

        // 채팅 시스템 메시지 저장 + 브로드캐스트
        Long roomId = change.getChatRoom().getId();

        String text = "예약 일정 변경 요청이 수락되었습니다. 변경 일정은 예약 내역에서 확인하실 수 있습니다.";
        chattingService.publishReservationText(me.getId(), roomId, text);

        return new SimpleMessageDTO("예약이 변경되었습니다.");
    }

}
