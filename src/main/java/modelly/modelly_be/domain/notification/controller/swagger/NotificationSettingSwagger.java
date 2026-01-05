package modelly.modelly_be.domain.notification.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.notification.dto.request.NotificationSettingRequest;
import modelly.modelly_be.domain.notification.dto.response.NotificationSettingResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "알림 수신설정 관련 API")
public interface NotificationSettingSwagger {

    @Operation(summary = "알림 수신설정 조회 API", description = "사용자가 자신의 알림 수신설정을 조회할 때 사용하는 API입니다.")
    @GetMapping("/notification-setting")
    ApiResponse<NotificationSettingResponse> getNotificationSetting(@AuthenticationPrincipal AuthDetails authDetails);

    @Operation(summary = "알림 수신설정 수정 API", description = """
            사용자가 수신 설정을 수정할 때 사용하는 API 입니다 \n
            ### Request Param
            `chattingNotification` : 채팅 알림 수신 설정 \n
            `reservationNotification` : 예약 알림 수신 설정 \n
            `scheduleNotification` : 일정 알림 수신 설정 \n
            `reviewNotification` : 리뷰 알림 수신 설정 \n
            """)
    @PutMapping("/notification-setting")
    ApiResponse<String> updateNotificationSetting(@AuthenticationPrincipal AuthDetails authDetails, @RequestBody NotificationSettingRequest notificationSettingRequest);

}
