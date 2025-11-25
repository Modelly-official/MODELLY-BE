package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecruitmentImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @Column(name = "image_url", nullable = false, length = 254)
    private String imageUrl;
}
