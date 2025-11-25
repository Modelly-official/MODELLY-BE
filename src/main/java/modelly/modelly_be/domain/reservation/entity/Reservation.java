package modelly.modelly_be.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Reservation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "designer_name", length = 20)
    private String designerName;

    @Column(name = "shop", length = 100)
    private String shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus status;

    @Column(name = "cancel_reason", length = 100)
    private String cancelReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id")
    private Designer designer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;
}
