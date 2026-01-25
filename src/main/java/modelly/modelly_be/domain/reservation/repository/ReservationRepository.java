package modelly.modelly_be.domain.reservation.repository;

import jakarta.persistence.LockModeType;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    // date에 해당하는 예약 조회 (모델이 null이 아닌 경우 추가)
    List<Reservation> findAllByDesigner_User_IdAndDateAndStatusAndModelIsNotNullOrderByStartTimeAsc(
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
    List<Reservation> findAllByDate(LocalDate date);

    // 디자이너가 포함된 예약의 designer 필드를 null로 설정(디자이너 탈퇴 시 이용)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Reservation r SET r.designer = NULL WHERE r.designer.id = :designerId")
    void setDesignerNull(@Param("designerId") Long designerId);

    // 모델이 포함된 예약의 model 필드를 null로 설정(모델 탈퇴 시 이용)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Reservation r SET r.model = NULL WHERE r.model.id = :modelId")
    void setModelNull(@Param("modelId") Long modelId);

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.designer " +
            "JOIN FETCH r.recruitment rec " +
            "WHERE r.model = :model AND r.status = :status " +
            "ORDER BY r.date ASC " +
            "LIMIT 5")
    List<Reservation> findTop5ByModelAndStatus(
            @Param("model") Model model,
            @Param("status") ReservationStatus status
    );

    // 디자이너의 PENDING(대기중) 예약 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Reservation r WHERE r.designer.id = :designerId AND r.status = 'RESERVATION_PENDING'")
    void deletePendingByDesignerId(@Param("designerId") Long designerId);

    // 모델의 PENDING(대기중) 예약 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Reservation r WHERE r.model.id = :modelId AND r.status = 'RESERVATION_PENDING'")
    void deletePendingByModelId(@Param("modelId") Long modelId);

    // 해당 디자이너의 예약과 공고와의 연결 끊기
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Reservation r SET r.recruitment = NULL WHERE r.designer.id = :designerId")
    void setRecruitmentNullByDesignerId(@Param("designerId") Long designerId);
}
