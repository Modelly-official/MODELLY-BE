package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.global.entity.BaseEntity;

@Getter
@Builder
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecruitmentImage extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @Column(name = "image_url", nullable = false, length = 254)
    private String imageUrl;
}
