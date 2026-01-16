package modelly.modelly_be.domain.reservation.repository;

import jakarta.persistence.LockModeType;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.user.entity.Designer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsReservationByRecruitmentAndStatus(Recruitment recruitment, ReservationStatus reservationStatus);

    void deleteAllByRecruitment(Recruitment recruitment);

    List<Reservation> findAllByRecruitment(Recruitment recruitment);

    // 모델이 해당 공고에 예약 신청을 했는지 확인(중복 신청 방지)
    boolean existsByModel_IdAndRecruitment_IdAndStatusIn(
            Long modelId,
            Long recruitmentId,
            Collection<ReservationStatus> statuses
    );

    // 모델 아이디와 디자이너 아이디로 특정 예약 조회
    @Query("""
    select r
    from Reservation r
    where r.model.id = :modelId
      and r.designer.id = :designerId
      and r.status = modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus.RESERVATION_CONFIRMED
      and (
            r.date > :today
         or (r.date = :today and r.endTime >= :now)
      )
    order by r.date asc, r.startTime asc, r.id asc
    """)
    List<Reservation> findUpcomingConfirmedForChatRoom(
            @Param("modelId") Long modelId,
            @Param("designerId") Long designerId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now,
            Pageable pageable
    );

    // 예약 변경 시 변경 시간대에 예약이 있는지 확인
    @Query("""
    select (count(r) > 0)
    from Reservation r
    where r.designer.id = :designerId
      and r.date = :date
      and r.status in :statuses
      and r.startTime < :endTime
      and r.endTime > :startTime
      and r.id <> :excludeReservationId
    """)
    boolean existsConflictOnDesignerSchedule(
            @Param("designerId") Long designerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") List<ReservationStatus> statuses,
            @Param("excludeReservationId") Long excludeReservationId
    );

    // date에 해당하는 예약 조회
    List<Reservation> findAllByDesigner_User_IdAndDateAndStatusOrderByStartTimeAsc(
            Long designerUserId,
            LocalDate date,
            ReservationStatus status
    );

    // 예약 조회(Lock)
    // 모델의 예약 신청 취소 시 이용, Designer가 예약 확정을 해버리는 순간에 예약 거절을 누를 수도 있으니 필요
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.model " +
            "JOIN FETCH r.designer " +
            "WHERE r.date = :date ")
    List<Reservation> findAllByStartTime(LocalDate date);
}
