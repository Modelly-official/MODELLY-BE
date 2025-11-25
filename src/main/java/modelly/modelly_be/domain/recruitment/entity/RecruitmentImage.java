package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecruitmentImage {
    @Id
    @Column(name = "recruitment_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;
}
