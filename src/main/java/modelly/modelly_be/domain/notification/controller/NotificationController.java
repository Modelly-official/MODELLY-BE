package modelly.modelly_be.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.controller.swagger.NotificationSwagger;
import modelly.modelly_be.domain.notification.dto.request.FcmTokenRequest;
import modelly.modelly_be.domain.notification.dto.response.NotificationListResponse;
import modelly.modelly_be.domain.notification.dto.response.UnreadNotificationResponse;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.notification.service.FcmTokenService;
import modelly.modelly_be.domain.notification.service.NotificationService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationSwagger {

    private final NotificationService notificationService;
    private final FcmTokenService fcmTokenService;


    //fcm 토큰 저장
    @Override
    public ApiResponse<String> saveFcmToken(AuthDetails authDetails, FcmTokenRequest fcmTokenRequest) {
        fcmTokenService.saveOrUpdateToken(authDetails.user().getId(), fcmTokenRequest.fcmToken());

        return ApiResponse.onSuccess("FCM 토큰을 저장했습니다.");
    }

    //알림 리스트 조회
    @Override
    public ApiResponse<ScrollResponse<NotificationListResponse>> getNotifications(AuthDetails authDetails, NotificationType notificationType, Long cursorId, int size) {

        ScrollResponse<NotificationListResponse> response = notificationService.getNotifications(authDetails.user(), notificationType, cursorId, size);

        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<UnreadNotificationResponse> getUnreadNotifications(AuthDetails authDetails) {
        UnreadNotificationResponse response = notificationService.countUnreadNotification(authDetails.user());

        return ApiResponse.onSuccess(response);
    }
}
