package modelly.modelly_be.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class CronScheduler {

    private final RecruitmentService recruitmentService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void updatePostStatus(){
        try{
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            int updated = recruitmentService.updateStatusToClosed(today);
            log.info("[scheduler] 마감기한이 지난 공고글 상태 업데이트 -> 총 {}건", updated);
        } catch (Exception e){
            log.error("[scheduler] 🚨ERROR | message: {}", e.getMessage());
        }

    }
}
