package modelly.modelly_be.domain.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;
}
