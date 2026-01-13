package modelly.modelly_be.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.global.entity.BaseEntity;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Reservation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", columnDefinition = "TIME", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", columnDefinition = "TIME", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name="category", nullable=false, length=20)
    private Category category;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "reservation_sub_category",
            joinColumns = @JoinColumn(name = "reservation_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", length = 50)
    private List<SubCategory> subCategories;

    @Column(name = "designer_name", length = 20)
    private String designerName;

    @Column(name = "shop", length = 100)
    private String shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus status;

    @Column(name = "cancel_reason", length = 100)
    private String cancelReason;

    @Column(name = "image_url", length = 254)
    private String imageUrl;

    @Column(name = "comment", nullable = false, length = 254)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", nullable = true)
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id")
    private Designer designer;

    public void applySchedule(LocalDate date, LocalTime start, LocalTime end) {
        this.date = date;
        this.startTime = start;
        this.endTime = end;
    }

    public void deleteRelationShip(){
        this.recruitment = null;
    }

    public void cancel(String reason) {
        this.status = ReservationStatus.RESERVATION_CANCELLED;
        this.cancelReason = reason;
    }

    public void confirm() {
        this.status = ReservationStatus.RESERVATION_CONFIRMED;
    }

    public void reject() {
        this.status = ReservationStatus.RESERVATION_CANCELLED;
        this.cancelReason = null;
    }
}
