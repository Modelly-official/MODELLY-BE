package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Table(name = "recruitment_date")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecruitmentDate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_date_id")
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

    @Column(name = "is_reserved", nullable = false)
    private boolean isReserved = true;

    @OneToMany(mappedBy = "recruitmentDate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecruitmentTime> recruitmentTimes = new ArrayList<>();

    public void addTime(RecruitmentTime time) {
        recruitmentTimes.add(time);
    }
}
