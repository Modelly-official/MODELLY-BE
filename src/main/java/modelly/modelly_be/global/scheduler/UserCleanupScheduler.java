package modelly.modelly_be.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.domain.user.service.UserHardDeleteService;
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
    private final UserHardDeleteService userHardDeleteService;

    // 데이터 삭제 시간 설정(매일 자정)
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void deleteExpiredUsers() {
        log.info("=== 탈퇴 유저 영구 삭제 스케줄러 시작 ===");

        // 기준 시간(5일) 설정 후 탈퇴 후 5일이 지난 유저 찾기
        LocalDateTime expirationTime = LocalDateTime.now().minusDays(5);
        List<User> expiredUsers = userRepository.findByDeletedAtBefore(expirationTime);

        if (expiredUsers.isEmpty()) {
            log.info("삭제할 대상이 없습니다.");
            return;
        }

        log.info("총 {}명의 삭제 대상을 발견했습니다.", expiredUsers.size());

        int successCount = 0;
        int failCount = 0;

        // 한 명씩 로직을 태워서 삭제 (한 명 실패해도 나머지는 계속 진행)
        for (User user : expiredUsers) {
            try {
                userHardDeleteService.deleteUser(user);
                successCount++;
            } catch (Exception e) {
                log.error("유저 삭제 실패 (userId={}): {}", user.getId(), e.getMessage());
                failCount++;
            }
        }

        log.info("=== 탈퇴 유저 정리 완료 (성공: {}, 실패: {}) ===", successCount, failCount);
    }
}
