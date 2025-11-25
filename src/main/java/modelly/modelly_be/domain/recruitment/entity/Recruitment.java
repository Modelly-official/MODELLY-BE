package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.entity.BaseEntity;
import modelly.modelly_be.global.entity.Category;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Recruitment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "content", nullable = false, length = 255)
    private String content;

    @Column(name = "notice", nullable = false, length = 255)
    private String notice;

    @Column(name = "goal1", length = 255)
    private String goal1;

    @Column(name = "goal2", length = 255)
    private String goal2;

    @Column(name = "goal3", length = 255)
    private String goal3;

    @Column(name = "agree_video")
    private boolean agreeVideo;

    @Column(name = "agree_insta")
    private boolean agreeInsta;

    @Column(name = "agree_mosaic")
    private boolean agreeMosaic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id")
    private Designer designer;
}
