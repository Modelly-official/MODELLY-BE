package modelly.modelly_be.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "shop_address", length = 255)
    private String shopAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "chemistry_score")
    private Long chemistryScore;

    @Column(name = "nickname", length = 20, nullable = false)                                     // VARCHAR(20)
    private String nickname;

    @Column(name = "instagram_id", length = 50)                                     // VARCHAR(20)
    private String instagramId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder
    public Designer(String shop, String shopAddress, Category category, Long chemistryScore, String nickname, String instagramId, User user) {
        this.shop = shop;
        this.shopAddress = shopAddress;
        this.category = category;
        this.chemistryScore = chemistryScore;
        this.nickname = nickname;
        this.instagramId = instagramId;
        this.user = user;
    }
}