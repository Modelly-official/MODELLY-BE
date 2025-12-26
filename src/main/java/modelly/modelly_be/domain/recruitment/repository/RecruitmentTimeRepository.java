package modelly.modelly_be.domain.recruitment.repository;

import jakarta.persistence.LockModeType;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface RecruitmentTimeRepository extends JpaRepository<RecruitmentTime, Long> {

    // recruitmentTime을 기준으로 Lock (디자이너 기준으로 동일 시간대 모두 Lock)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select rt
        from RecruitmentTime rt
        join rt.recruitmentDate rd
        join rd.recruitment r
        where r.designer.id = :designerId
          and rd.date = :date
          and rt.startTime = :startTime
    """)
    List<RecruitmentTime> findAllTimeForUpdateByDesigner(
            Long designerId,
            LocalDate date,
            LocalTime startTime
    );
}
