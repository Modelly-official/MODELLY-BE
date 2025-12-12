package modelly.modelly_be.domain.review.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.review.dto.request.ReviewUpdateRequestDto;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name = "review",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"reservation_id", "model_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "summary", nullable = false, length = 50)
    private String summary;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "is_fixed", nullable = false)
    private boolean isFixed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id")
    private Designer designer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;

    @Column(name = "rating", nullable = false)
    private float rating;

    @Column(name = "thumbnail")
    private String thumbnail;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> reviewImages = new ArrayList<>();

    public void addReviewImage(ReviewImage reviewImage) {
        this.reviewImages.add(reviewImage);
    }

    public void update(ReviewUpdateRequestDto requestDto) {
        if (requestDto.rating() != null) rating = requestDto.rating();
        if (requestDto.content() != null) content = requestDto.content();
    }
}
