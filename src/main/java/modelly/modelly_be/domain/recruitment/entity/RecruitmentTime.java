package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentTime {
    @Id
    @Column(name = "recruitment_date_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "recruitment_date_id")
    private RecruitmentDate recruitmentDate;

    @Column(name = "start_time", length = 20, nullable = false)
    private String startTime;
}
