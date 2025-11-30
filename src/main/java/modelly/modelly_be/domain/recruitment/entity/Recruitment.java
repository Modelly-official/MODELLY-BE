package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.recruitment.dto.request.UpdateRecruitmentRequestDto;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.entity.BaseEntity;
import modelly.modelly_be.global.entity.Category;

import java.util.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", nullable = false)
    private SubCategory subCategory;

    @Column(name = "content", nullable = false, length = 254)
    private String content;

    @Column(name = "notice", nullable = false, length = 254)
    private String notice;

    @Column(name = "goal1", length = 254)
    private String goal1;

    @Column(name = "goal2", length = 254)
    private String goal2;

    @Column(name = "goal3", length = 254)
    private String goal3;

    @Column(name = "agree_video")
    private boolean agreeVideo;

    @Column(name = "agree_insta")
    private boolean agreeInsta;

    @Column(name = "agree_mosaic")
    private boolean agreeMosaic;

    @Column(name = "etc", length = 100)
    private String etc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id")
    private Designer designer;

    @OneToMany(mappedBy = "recruitment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RecruitmentDate> recruitmentDates = new HashSet<>();

    @OneToMany(mappedBy = "recruitment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private Set<RecruitmentImage> recruitmentImages = new LinkedHashSet<>();

    public void addDate(RecruitmentDate date) {
        recruitmentDates.add(date);
    }

    public void addImage(RecruitmentImage img) {
        recruitmentImages.add(img);
    }

    public void updateRecruitment(UpdateRecruitmentRequestDto dto) {
        if (dto.title() != null) this.title = dto.title();
        if (dto.category() != null) this.category = dto.category();
        if (dto.content() != null) this.content = dto.content();
        if (dto.notice() != null) this.notice = dto.notice();
        this.goal1 = dto.goal1();
        this.goal2 = dto.goal2();
        this.goal3 = dto.goal3();
        this.agreeVideo = dto.agreeVideo();
        this.agreeInsta = dto.agreeInsta();
        this.agreeMosaic = dto.agreeMosaic();
        this.etc = dto.etc();
    }
}
