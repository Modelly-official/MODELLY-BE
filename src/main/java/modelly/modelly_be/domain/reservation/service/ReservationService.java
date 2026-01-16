package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.response.*;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.service.ChatRoomService;
import modelly.modelly_be.domain.chat.service.ChattingService;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import modelly.modelly_be.domain.recruitment.repository.RecruitmentTimeRepository;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCancelRequest;
import modelly.modelly_be.domain.reservation.dto.request.ReservationChangeCreateRequest;
import modelly.modelly_be.domain.reservation.dto.internal.ChatRoomReservationSummary;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationChangeRepository reservationChangeRepository;
    private final RecruitmentTimeRepository recruitmentTimeRepository;

    private final ChatRoomService chatRoomService;
    private final ChattingService chattingService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private final ReservationNotificationService reservationNotificationService;
    private final ScheduleNotificationService scheduleNotificationService;

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

        // 전역 슬롯 RecruitmentTime 락 + 비어있는지 체크
        Long designerId = reservation.getDesigner().getId();
        List<RecruitmentTime> proposedTimes =
                recruitmentTimeRepository.findAllTimeForUpdateByDesigner(
                        designerId, req.proposedDate(), proposedStart
                );

        // 요청 시간대가 디자이너가 정한 모든 공고 시간대에 없으면 요청 불가
        if (proposedTimes.isEmpty()) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        // 변경하려는 시간대가 현재 예약과 연결된 공고의 time slot인지 확인
        Long recruitmentId = reservation.getRecruitment() != null ? reservation.getRecruitment().getId() : null;
        if (recruitmentId == null) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }
        boolean hasMyRecruitmentSlot = proposedTimes.stream().anyMatch(rt ->
                rt.getRecruitmentDate().getRecruitment().getId().equals(recruitmentId)
        );
        if (!hasMyRecruitmentSlot) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        // 하나라도 isReserved=true면 요청 불가(예약된 시간대면 요청 불가)
        boolean reservedAny = proposedTimes.stream().anyMatch(RecruitmentTime::isReserved);
        if (reservedAny) {
            throw new GeneralException(ErrorStatus.RESERVATION_TIME_CONFLICT);
        }

        // pending change 슬롯 겹침 방지
        boolean pendingConflict = reservationChangeRepository.existsByReservation_Designer_IdAndStatusAndProposedDateAndProposedStartTime(
                designerId,
                ReservationChangeStatus.PENDING,
                req.proposedDate(),
                proposedStart
        );

        if (pendingConflict) {
            throw new GeneralException(ErrorStatus.RESERVATION_TIME_CONFLICT);
        }

        // 실제 Reservation(confirmed/pending) 스케줄 겹침 체크
        boolean conflict = reservationRepository.existsConflictOnDesignerSchedule(
                designerId,
                req.proposedDate(),
                proposedStart,
                proposedEnd,
                List.of(ReservationStatus.RESERVATION_CONFIRMED, ReservationStatus.RESERVATION_PENDING),
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

        User opponantUser = meIsModel
                ? reservation.getDesigner().getUser()
                : reservation.getModel().getUser();

        if (opponantUser.getNotificationSetting().isScheduleNotification()){
            scheduleNotificationService.createScheduleChangeNotification(opponantUser, finalRoomId, reservation);
        }

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


        // 예약이 공고와 연결되어 있어야 슬롯 검증 가능
        Recruitment recruitment = reservation.getRecruitment();
        if (recruitment == null) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        Long designerId = reservation.getDesigner().getId();

        // old time slot(현재 예약의 slot)
        LocalDate oldDate = reservation.getDate();
        LocalTime oldStart = reservation.getStartTime();

        // new time slot(변경 요청 slot)
        LocalDate newDate = change.getProposedDate();
        LocalTime newStart = change.getProposedStartTime();

        // new slot 전역 RecruitmentTime row들 전부 락 + 검증
        List<RecruitmentTime> newTimes = recruitmentTimeRepository
                .findAllTimeForUpdateByDesigner(designerId, newDate, newStart);

        // new 슬롯이 어떤 공고에도 없으면 불가
        if (newTimes.isEmpty()) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        // newTimes 중 현재 예약의 recruitment에 해당하는 row가 포함되었는지 검증
        boolean hasMyRecruitmentSlotInNew = newTimes.stream().anyMatch(rt ->
                rt.getRecruitmentDate().getRecruitment().getId().equals(recruitment.getId())
        );
        if (!hasMyRecruitmentSlotInNew) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        // new slot이 하나라도 reserved=true면 수락 불가
        boolean reservedAny = newTimes.stream().anyMatch(RecruitmentTime::isReserved);
        if (reservedAny) {
            throw new GeneralException(ErrorStatus.RESERVATION_TIME_CONFLICT);
        }

        // Reservation 스케줄 conflict 재검증 (CONFIRMED + PENDING 기준, 자기 예약 제외)
        boolean conflict = reservationRepository.existsConflictOnDesignerSchedule(
                designerId,
                newDate,
                change.getProposedStartTime(),
                change.getProposedEndTime(),
                List.of(ReservationStatus.RESERVATION_CONFIRMED, ReservationStatus.RESERVATION_PENDING),
                reservation.getId()
        );
        if (conflict) {
            throw new GeneralException(ErrorStatus.RESERVATION_TIME_CONFLICT);
        }

        // old 슬롯 전역 RecruitmentTime row들 전부 락
        List<RecruitmentTime> oldTimes = recruitmentTimeRepository
                .findAllTimeForUpdateByDesigner(designerId, oldDate, oldStart);

        // old 전부 unreserve, new 전부 reserve
        oldTimes.forEach(RecruitmentTime::unreserve);
        newTimes.forEach(RecruitmentTime::reserve);

        // 예약에 반영
        reservation.applySchedule(
                change.getProposedDate(),
                change.getProposedStartTime(),
                change.getProposedEndTime()
        );

        // 변경요청 ACCEPTED 처리
        change.accept(me.getId());

        //  채팅 시스템 메시지 저장 + 브로드캐스트
        Long roomId = change.getChatRoom().getId();
        String text = "예약 일정 변경 요청이 수락되었습니다. 변경 일정은 예약 내역에서 확인하실 수 있습니다.";
        chattingService.publishTextMessage(me.getId(), roomId, text);

        return new SimpleMessageDTO("예약이 변경되었습니다.");
    }

    // 예약 변경 요청 취소
    @Transactional
    public SimpleMessageDTO cancelReservationChange(Long changeId, User me) {

        // 변경요청 조회
        ReservationChange change = reservationChangeRepository.findById(changeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION_CHANGE));

        // PENDING만 취소 가능
        if (!change.isPending()) {
            throw new GeneralException(ErrorStatus.RESERVATION_CHANGE_NOT_PENDING);
        }

        // 요청자 본인만 취소 가능
        if (!change.getRequesterUserId().equals(me.getId())) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        // 상태 변경
        change.cancel(me.getId());

        // 채팅 시스템 메시지 저장 + 브로드캐스트
        Long roomId = change.getChatRoom().getId();
        Reservation reservation = change.getReservation();

        ReservationChangeCancelChatPayload payload = new ReservationChangeCancelChatPayload(
                "CHANGE_CANCEL",
                change.getId(),
                reservation.getId(),
                reservation.getDate(),
                reservation.getStartTime().format(HM),
                reservation.getEndTime().format(HM),
                "변경 요청이 취소되었습니다."
        );

        chattingService.publishReservationPayload(me.getId(), roomId, payload);

        return new SimpleMessageDTO("변경 요청이 취소되었습니다.");
    }

    // 예약 취소
    @Transactional
    public SimpleMessageDTO cancelReservation(
            Long reservationId,
            Long roomId,      // nullable
            Long changeId, // nullable
            User me,
            ReservationCancelRequest req
    ) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION));

        // 참여자 검증
        boolean meIsModel = reservation.getModel() != null
                && reservation.getModel().getUser().getId().equals(me.getId());
        boolean meIsDesigner = reservation.getDesigner() != null
                && reservation.getDesigner().getUser().getId().equals(me.getId());

        if (!meIsModel && !meIsDesigner) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        // 이미 취소면 방어
        if (reservation.getStatus() == ReservationStatus.RESERVATION_CANCELLED) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        // 72시간(3일) 전까지만 취소 가능
        LocalDateTime now = LocalDateTime.now(KST);
        LocalDateTime startAt = LocalDateTime.of(reservation.getDate(), reservation.getStartTime());
        long hoursUntilStart = Duration.between(now, startAt).toHours();

        // 예약 시작이 이미 지났거나, 72시간 미만으로 남았으면 취소 불가
        if (hoursUntilStart < 72) {
            throw new GeneralException(ErrorStatus.RESERVATION_CANCEL_TOO_LATE);
        }

        // changeId가 있으면 예약 변경 요청 거절 후 취소는 요청자만 가능
        if (changeId != null) {
            ReservationChange change = reservationChangeRepository.findById(changeId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION_CHANGE));

            // change가 이 reservation의 change가 맞는지
            if (!change.getReservation().getId().equals(reservationId)) {
                throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
            }

            // 거절된 change만 해당 플로우 허용
            if (change.getStatus() != ReservationChangeStatus.REJECTED) {
                throw new GeneralException(ErrorStatus.RESERVATION_CHANGE_NOT_REJECTED);
            }

            // 요청자만 취소 가능
            if (!change.getRequesterUserId().equals(me.getId())) {
                throw new GeneralException(ErrorStatus.RESERVATION_CHANGE_ONLY_REQUESTER_CAN_PROCEED_OR_CANCEL);
            }
        }

        // roomId 없으면 채팅방 오픈(없으면 생성)
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

        // RecruitmentTime 전역 슬롯 해제
        Long designerId = reservation.getDesigner().getId();
        LocalDate date = reservation.getDate();
        LocalTime start = reservation.getStartTime();

        List<RecruitmentTime> times = recruitmentTimeRepository
                .findAllTimeForUpdateByDesigner(designerId, date, start);

        if (times.isEmpty()) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        // 전부 예약 가능 상태로 변경
        times.forEach(RecruitmentTime::unreserve);

        //  Reservation 취소 반영
        reservation.cancel(req.reason());

        // 채팅 브로드캐스트
        String notice = "예약이 취소되었어요.\n해당 시간대에 다시 예약 신청을 받을 수 있습니다.";

        ReservationCancelChatPayload payload = new ReservationCancelChatPayload(
                "RESERVATION_CANCEL",
                reservation.getId(),
                reservation.getDate(),
                reservation.getStartTime().format(HM),
                reservation.getEndTime().format(HM),
                req.reason(),
                notice
        );

        chattingService.publishReservationPayload(me.getId(), finalRoomId, payload);

        User opponentUser = meIsModel
                ? reservation.getDesigner().getUser()
                : reservation.getModel().getUser();

        if (opponentUser.getNotificationSetting().isScheduleNotification()){
            scheduleNotificationService.createScheduleCancelNotification(me, reservation, finalRoomId);
        }

        return new SimpleMessageDTO("예약이 취소되었습니다.");
    }

    // 예약 변경 요청 거절
    @Transactional
    public SimpleMessageDTO rejectReservationChange(Long changeId, User me) {

        ReservationChange change = reservationChangeRepository.findById(changeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION_CHANGE));

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

        // 요청자가 응답(수락/거절)하는 거 방지
        if (change.getRequesterUserId().equals(me.getId())) {
            throw new GeneralException(ErrorStatus.RESERVATION_CHANGE_SELF_RESPONSE_NOT_ALLOWED);
        }

        // 확정 예약만 응답 허용
        if (reservation.getStatus() != ReservationStatus.RESERVATION_CONFIRMED) {
            throw new GeneralException(ErrorStatus.RESERVATION_BAD_REQUEST);
        }

        // 상태 변경
        change.reject(me.getId());

        // 채팅 payload
        Long roomId = change.getChatRoom().getId();

        String text = "예약 일정 변경 요청이 거절되었습니다. 기존 예약 일정 진행 여부를 선택해주세요.";
        ReservationSimpleTextPayload payload = new ReservationSimpleTextPayload(
                "CHANGE_REJECTED",
                changeId,
                text
        );
        chattingService.publishReservationPayload(me.getId(), roomId, payload);

        return new SimpleMessageDTO("예약 변경 요청을 거절했습니다.");
    }

    // 기존대로 진행 (변경 요청 거절 후)
    @Transactional
    public SimpleMessageDTO proceedReservation(Long changeId, User me) {

        ReservationChange change = reservationChangeRepository.findById(changeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_RESERVATION_CHANGE));

        // 거절된 요청에 대해서만 가능
        if (change.getStatus() != ReservationChangeStatus.REJECTED) {
            throw new GeneralException(ErrorStatus.RESERVATION_CHANGE_NOT_REJECTED);
        }

        // 요청자만 "기존대로 진행" 가능
        if (!change.getRequesterUserId().equals(me.getId())) {
            throw new GeneralException(ErrorStatus.RESERVATION_CHANGE_ONLY_REQUESTER_CAN_PROCEED_OR_CANCEL);
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

        Long roomId = change.getChatRoom().getId();

        String notice = "변경 없이 기존 예약 일정으로 진행합니다.";

        ReservationProceedChatPayload payload = new ReservationProceedChatPayload(
                "CHANGE_PROCEED",
                change.getId(),
                reservation.getId(),
                reservation.getDate(),
                reservation.getStartTime().format(HM),
                reservation.getEndTime().format(HM),
                notice
        );

        // 채팅 저장 + 브로드캐스트
        chattingService.publishReservationPayload(me.getId(), roomId, payload);

        return new SimpleMessageDTO("기존 예약 일정으로 진행합니다.");
    }

    public YearMonth parseYearMonthOrNow(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now(KST);
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new GeneralException(ErrorStatus.MONTH_BAD_REQUEST);
        }
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllByStartDate(LocalDate oneDaysLater) {
        return reservationRepository.findAllByStartTime(oneDaysLater);
    }
}
