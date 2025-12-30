package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_date_id")
    private RecruitmentDate recruitmentDate;

    @Column(name = "is_reserved", nullable = false)
    private boolean isReserved = true;

    @Column(name = "start_time", columnDefinition = "TIME", nullable = false)
    private LocalTime startTime;

    public void reserve() { this.isReserved = true; }
    public void unreserve() { this.isReserved = false; }
}
