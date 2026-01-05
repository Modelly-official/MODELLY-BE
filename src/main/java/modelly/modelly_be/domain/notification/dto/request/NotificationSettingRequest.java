package modelly.modelly_be.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationSettingRequest(
        @Schema(description = "채팅 알림", example = "true")
        boolean chattingNotification,
        @Schema(description = "예약 알림", example = "true")
        boolean reservationNotification,
        @Schema(description = "일정 알림", example = "true")
        boolean scheduleNotification,
        @Schema(description = "리뷰 알림", example = "true")
        boolean reviewNotification
) {
}
