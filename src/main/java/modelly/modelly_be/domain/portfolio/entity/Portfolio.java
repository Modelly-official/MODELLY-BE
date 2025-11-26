package modelly.modelly_be.domain.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.entity.BaseEntity;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    @Column(name = "image_url", length = 254, nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id")
    private Designer designer;
}
