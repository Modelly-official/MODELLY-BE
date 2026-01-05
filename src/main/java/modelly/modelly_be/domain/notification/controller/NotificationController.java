package modelly.modelly_be.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.controller.swagger.NotificationSwagger;
import modelly.modelly_be.domain.notification.dto.response.NotificationListResponse;
import modelly.modelly_be.domain.notification.entity.NotificationType;
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

    @Override
    public ApiResponse<ScrollResponse<NotificationListResponse>> getNotifications(AuthDetails authDetails, NotificationType notificationType, Long cursorId, int size) {

        List<NotificationListResponse> notificationList = notificationService.getNotifications(authDetails.user(), notificationType, cursorId, size);

        ScrollResponse<NotificationListResponse> response = ScrollUtil.paginate(notificationList, size);

        return ApiResponse.onSuccess(response);
    }
}
