package modelly.modelly_be.domain.user.repository;

import modelly.modelly_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNum(String phoneNum);

    // 이메일 인증 시 이용
    Optional<User> findByNameAndEmail(String name, String email);
    Optional<User> findByNameAndLoginIdAndEmail(String name, String loginId, String email);

    // 이메일로 찾기
    Optional<User> findByEmail(String email);

    // 탈퇴일이 특정 날짜 이전인 유저 목록 조회
    List<User> findByDeletedAtBefore(LocalDateTime beforeDate);

}