package modelly.modelly_be.domain.like.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recruitment_like",
        uniqueConstraints =
            @UniqueConstraint(columnNames = {"recruitment_id", "model_id"}))
@AllArgsConstructor
public class RecruitmentLike extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;
}
