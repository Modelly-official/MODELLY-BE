package modelly.modelly_be.global.listener;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.global.listener.dto.S3FolderDeleteEvent;
import modelly.modelly_be.global.s3.S3Uploader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DeleteImageEventListener {
    private final S3Uploader s3Uploader;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleS3FolderDelete(S3FolderDeleteEvent event){
        s3Uploader.deleteFolder(event.folderPath());
    }
}
