package modelly.modelly_be.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.BaseEntity;

@Entity
@Table(name = "designer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Designer extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designer_id")
    private Long id;

    @Column(name = "shop", length = 50)
    private String shop;

    @Column(name = "address_line1", length = 50)
    private String addressLine1;

    @Column(name = "address_line2", length = 50)
    private String addressLine2;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "chemistry_score")
    private Long chemistryScore;

    @Column(name = "nickname", length = 20, nullable = false)                                     // VARCHAR(20)
    private String nickname;

    @Column(name = "instagram_id", length = 50)                                     // VARCHAR(20)
    private String instagramId;

    @Column(name = "intro", length = 100, nullable = false)                                     // VARCHAR(20)
    private String intro;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "review_count", nullable = false)
    private Long reviewCount= 0L;

    @Column(name = "reservation_count", nullable = false)
    private Long reservationCount= 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount= 0L;

    @Builder
    public Designer(
            String shop,
            String addressLine1,
            String addressLine2,
            Double latitude,
            Double longitude,
            Category category,
            Long chemistryScore,
            String nickname,
            String instagramId,
            String intro,
            User user
    ) {
        this.shop = shop;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.chemistryScore = chemistryScore;
        this.nickname = nickname;
        this.instagramId = instagramId;
        this.intro = intro;
        this.user = user;
    }
    public void updateProfile(
            String nickname,
            String intro,
            String shop,
            String addressLine1,
            String addressLine2
    ) {
        this.nickname = nickname;
        this.intro = intro;
        this.shop = shop;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
    }

    public void updateMyPage(
            String nickname,
            String intro,
            String shop,
            String addressLine1,
            String addressLine2,
            Category category
    ) {
        this.nickname = nickname;
        this.intro = intro;
        this.shop = shop;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.category = category;
    }

}