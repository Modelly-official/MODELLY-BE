package modelly.modelly_be.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    // 데이터 삭제 시간
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteExpiredUsers() {
        log.info("=== 탈퇴 유저 영구 삭제 스케줄러 시작 ===");

        // 기준 시간: 현재로부터 5일 전
        LocalDateTime expirationTime = LocalDateTime.now().minusDays(5);

        // 5일 전에 deletedAt이 찍힌 유저들 조회
        List<User> expiredUsers = userRepository.findByDeletedAtBefore(expirationTime);

        if (expiredUsers.isEmpty()) {
            log.info("삭제할 대상이 없습니다.");
            return;
        }

        log.info("총 {}명의 유저를 영구 삭제합니다.", expiredUsers.size());

        // 데이터 영구 삭제
        userRepository.deleteAll(expiredUsers);

        log.info("=== 탈퇴 유저 정리 완료 ===");
    }
}
