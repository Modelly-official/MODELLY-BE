package modelly.modelly_be.global.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.global.listener.dto.S3FolderDeleteEvent;
import modelly.modelly_be.global.s3.S3Uploader;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteImageEventListener {
    private final S3Uploader s3Uploader;

    @Retryable(backoff = @Backoff(delay = 500, multiplier = 2.0))
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleS3FolderDelete(S3FolderDeleteEvent event){
        try {
            s3Uploader.deleteFolder(event.folderPath());
            log.info("[INFO] S3 folder {} deleted", event.folderPath());
        } catch (Exception e) {
            log.debug("[DEBUG] S3 folder failed to delete. message : {}", e.getMessage());
            throw e;
        }

    }

    @Recover
    public void recover(Exception e) {
        log.error("[ERROR] S3 folder failed to delete: {}", e);
    }
}
