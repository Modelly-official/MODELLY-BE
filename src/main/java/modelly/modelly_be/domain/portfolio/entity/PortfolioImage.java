package modelly.modelly_be.domain.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PortfolioImage extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_image_id")
    private Long id;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
}
