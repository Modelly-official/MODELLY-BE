package modelly.modelly_be.domain.review.entity;

import jakarta.persistence.*;
import lombok.*;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reply extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long id;

    @Column(name = "content", length = 200)
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "designer_id")
    private Designer designer;

    public void update(String content) {
        this.content = content;
    }
}
