package modelly.modelly_be.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.controller.swagger.NotificationSettingSwagger;
import modelly.modelly_be.domain.notification.dto.request.NotificationSettingRequest;
import modelly.modelly_be.domain.notification.dto.response.NotificationSettingResponse;
import modelly.modelly_be.domain.notification.entity.NotificationSetting;
import modelly.modelly_be.domain.notification.service.NotificationSettingService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationSettingController implements NotificationSettingSwagger {

    private final NotificationSettingService notificationSettingService;

    @Override
    public ApiResponse<NotificationSettingResponse> getNotificationSetting(AuthDetails authDetails) {

        NotificationSetting notificationSetting = notificationSettingService.getNotificationSetting(authDetails.user());

        return ApiResponse.onSuccess(NotificationSettingResponse.of(notificationSetting));
    }

    @Override
    public ApiResponse<String> updateNotificationSetting(AuthDetails authDetails, NotificationSettingRequest notificationSettingRequest) {
        notificationSettingService.updateNotificationSetting(authDetails.user(), notificationSettingRequest);

        return ApiResponse.onSuccess("수신설정이 수정되었습니다.");
    }


}
