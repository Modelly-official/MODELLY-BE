package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.recruitment.dto.request.UpdateRecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.entity.BaseEntity;
import modelly.modelly_be.global.entity.Category;

import java.time.LocalDate;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<SubCategory> subCategoryList = new HashSet<>();

    @Column(name = "content", nullable = false, length = 254)
    private String content;

    @Column(name = "notice", nullable = false, length = 254)
    private String notice;

    @Column(name = "restriction", nullable = false, length = 250)
    private String restriction;

    @Column(name = "goal1", length = 254)
    private String goal1;

    @Column(name = "agree_video")
    private boolean agreeVideo;

    @Column(name = "agree_insta")
    private boolean agreeInsta;

    @Column(name = "agree_mosaic")
    private boolean agreeMosaic;

    @Column(name = "etc", length = 100)
    private String etc;

    @Column(name = "thumbnail", length = 254)
    private String thumbnail;

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

    @Column(name = "deadline")
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    private RecruitmentStatus recruitmentStatus=RecruitmentStatus.OPEN;

    @Column(name = "folder_id", length = 36)
    private String imageFolderId;

    public void addDate(RecruitmentDate date) {
        recruitmentDates.add(date);
    }

    public void addImage(RecruitmentImage img) {
        recruitmentImages.add(img);
    }

    public void updateRecruitment(UpdateRecruitmentRequestDto dto) {
        if (dto.title() != null) this.title = dto.title();
        if (dto.content() != null) this.content = dto.content();
        if (dto.notice() != null) this.notice = dto.notice();
        this.goal1 = dto.goal1();
        this.agreeVideo = dto.agreeVideo();
        this.agreeInsta = dto.agreeInsta();
        this.agreeMosaic = dto.agreeMosaic();
        this.etc = dto.etc();
    }

    public void updateImageInf(String imageFolderId, String thumbnail) {
        this.imageFolderId = imageFolderId;
        this.thumbnail = thumbnail;
    }
}
