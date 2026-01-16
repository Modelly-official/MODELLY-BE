package modelly.modelly_be.domain.notification.service.mapping;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.notification.service.NotificationService;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewNotificationService {
    private final NotificationService notificationService;

    //디자이너한테 리뷰 알림 전송
    @Transactional
    public void createReviewNotification(User designer, String modelNickname, Long reviewId) {
        String title = modelNickname + "님이 작성하신 새로운 리뷰가 등록되었어요.";
        String message = "리뷰 관리에서 답글을 작성해보세요.";

        NotificationData notificationData = NotificationData.otherNotification(designer, NotificationType.REVIEW, title, message, reviewId);

        notificationService.createNotification(notificationData);
    }

    //모델한테 리뷰 답글 알림 전송
    @Transactional
    public void createReplyNotification(User model, String designerNickname, Long reviewId) {
        String title = "작성한 리뷰에 " + designerNickname + "디자이너님이 답글을 작성했어요.";
        String message = "리뷰 내역에서 확인해보세요.";

        NotificationData notificationData = NotificationData.otherNotification(model, NotificationType.REVIEW, title, message, reviewId);
        notificationService.createNotification(notificationData);
    }

}
