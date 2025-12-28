package modelly.modelly_be.domain.reservation.repository;

import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.user.entity.Designer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsReservationByRecruitmentAndStatus(Recruitment recruitment, ReservationStatus reservationStatus);

    void deleteAllByRecruitment(Recruitment recruitment);

    List<Reservation> findAllByRecruitment(Recruitment recruitment);

    // 동일 시간대 예약 확인(예약 상태가 PENDING, CONFIRMED인 경우만 고려)
    boolean existsByDesignerAndDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Designer designer,
            LocalDate date,
            Collection<ReservationStatus> statuses,
            LocalTime end,
            LocalTime start
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
            Long designerId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            List<ReservationStatus> statuses,
            Long excludeReservationId
    );

    // date에 해당하는 예약 조회
    List<Reservation> findAllByDesigner_User_IdAndDateAndStatusOrderByStartTimeAsc(
            Long designerUserId,
            LocalDate date,
            ReservationStatus status
    );
}
