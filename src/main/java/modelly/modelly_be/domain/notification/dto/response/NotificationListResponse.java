package modelly.modelly_be.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationListResponse(
        @Schema(description = "알림 id", example = "1")
        Long notificationId,
        @Schema(description = "알림 타입", example = "예약 확정")
        String notificationType,
        @Schema(description = "알림 내용", example = "예약이 확정되었습니다.")
        String content,
        @Schema(description = "알림이 전송된 시각", example = "어제")
        String createdAt,
        @Schema(description = "알림의 targetId", example = "1")
        Long targetId
) {
}
