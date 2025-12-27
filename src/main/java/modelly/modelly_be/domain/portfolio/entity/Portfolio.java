package modelly.modelly_be.domain.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.portfolio.dto.request.UpdatePortfolioRequest;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentImage;
import modelly.modelly_be.domain.recruitment.entity.enums.SubCategory;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.entity.BaseEntity;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<SubCategory> subCategoryList = new HashSet<>();

    @Column(name = "title", length = 20)
    private String title;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id")
    private Designer designer;

    @Column(name = "thumbnail", length = 254)
    private String thumbnail;

    @Column(name = "folder_id", length = 36)
    private String imageFolderId;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private Set<PortfolioImage> portfolioImages = new LinkedHashSet<>();

    public void updateImageInf(String imageFolderId, String thumbnail) {
        this.imageFolderId = imageFolderId;
        this.thumbnail = thumbnail;
    }

    public void addImage(PortfolioImage portfolioImage) {
        portfolioImages.add(portfolioImage);
    }

    public void updatePortfolio(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
