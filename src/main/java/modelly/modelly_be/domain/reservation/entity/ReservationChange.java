package modelly.modelly_be.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationChangeStatus;
import modelly.modelly_be.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReservationChange extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_change_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "requester_user_id", nullable = false)
    private Long requesterUserId;

    @Column(name = "responder_user_id")
    private Long responderUserId;

    // 변경 제안 날짜/시간
    @Column(name = "proposed_date", nullable = false)
    private LocalDate proposedDate;

    @Column(name = "proposed_start_time", columnDefinition = "TIME", nullable = false)
    private LocalTime proposedStartTime;

    @Column(name = "proposed_end_time", columnDefinition = "TIME", nullable = false)
    private LocalTime proposedEndTime;

    // 변경 사유
    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationChangeStatus status;

    // 수락/거절/취소 처리된 시각
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public void accept(Long responderUserId) {
        this.status = ReservationChangeStatus.ACCEPTED;
        this.responderUserId = responderUserId;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject(Long responderUserId) {
        this.status = ReservationChangeStatus.REJECTED;
        this.responderUserId = responderUserId;
        this.respondedAt = LocalDateTime.now();
    }

    public void cancel(Long requesterUserId) {
        this.status = ReservationChangeStatus.CANCELED;
        this.responderUserId = requesterUserId;
        this.respondedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == ReservationChangeStatus.PENDING;
    }
}