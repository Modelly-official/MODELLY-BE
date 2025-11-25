package modelly.modelly_be.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NotificationSetting extends BaseEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @Column(name = "chatting_notification")
    private boolean chattingNotification = true;

    @Builder.Default
    @Column(name = "reservation_notification")
    private boolean reservationNotification = true;

    @Builder.Default
    @Column(name = "schedule_notification")
    private boolean scheduleNotification = true;

    @Builder.Default
    @Column(name = "review_notification")
    private boolean reviewNotification = true;
}
