package modelly.modelly_be.domain.notification.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.notification.dto.request.FcmTokenRequest;
import modelly.modelly_be.domain.notification.dto.response.NotificationListResponse;
import modelly.modelly_be.domain.notification.dto.response.UnreadNotificationResponse;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "알림 관련 API", description = "알림 카테고리별 조회 API")
public interface NotificationSwagger {

    @PostMapping("/fcm-token")
    @Operation(summary = "fcm token 저장 API", description = """
            푸시알림을 위한 FCM 토큰을 저장하는 API입니다.
            ### Request Body
            `fcm token`: firebase로부터 발급받은 fcm 토큰
            """)
    ApiResponse<String> saveFcmToken(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestBody @Valid FcmTokenRequest fcmTokenRequest);

    @GetMapping("/notifications")
    @Operation(summary = "알림 카테고리별 조회 API", description = """
            사용자 (모델, 디자이너)가 알림을 카테고리별로 조회할 때 사용하는 API입니다. \n
            ### RequestParam
            `notificationType` : RESERVATION, CHATTING, REVIEW, SCHEDULE 중 택1, 전체 알림을 보고싶다면 선택X \n
            `cursorId` : 다음 알림 리스트를 가져올 때, 현재 응답의 nextCursor값을 넣어주세요. \n
            `size` : 한 페이지에서 보여질 알림의 개수 \n
            """)
    ApiResponse<ScrollResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) NotificationType notificationType,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/notifications/unread")
    @Operation(summary = "안 읽은 알림 개수 조회 API", description = "유저가 안 읽은 알림 개수를 조회할 때 사용하는 API입니다.")
    ApiResponse<UnreadNotificationResponse> getUnreadNotifications(@AuthenticationPrincipal AuthDetails authDetails);

}
