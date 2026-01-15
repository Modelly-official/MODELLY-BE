package modelly.modelly_be.domain.recruitment.repository;

import jakarta.persistence.LockModeType;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 해당 디자이너의 예약 가능한 시간대가 존재하는지 조회
    @Query("""
        select case when count(rt) > 0 then true else false end
        from RecruitmentTime rt
        where rt.recruitmentDate.recruitment.designer.id = :designerId
          and rt.recruitmentDate.date = :date
          and rt.startTime = :startTime
          and rt.isReserved = false
    """)
    boolean existsAvailableSlotForDesigner(
            @Param("designerId") Long designerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime
    );
}
