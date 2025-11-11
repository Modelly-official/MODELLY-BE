package modelly.modelly_be.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "designer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Designer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designer_id")
    private Long id;

    @Column(name = "shop", length = 50, nullable = false)
    private String shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    @Column(name = "chemistry_score")
    private Long chemistryScore;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder
    public Designer(String shop, Category category, Long chemistryScore, User user) {
        this.shop = shop;
        this.category = category;
        this.chemistryScore = chemistryScore;
        this.user = user;
    }
}