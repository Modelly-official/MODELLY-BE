package modelly.modelly_be.domain.notification.entity;

import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;

public enum NotificationType {
    RESERVATION("예약 알림"),
    CHATTING("채팅 알림"),
    REVIEW("리뷰 알림"),
    SCHEDULE("일정 알림");

    private final String description;

    NotificationType(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }

    public String toDisplayReviewType(UserRole userRole) {
        return userRole == UserRole.DESIGNER ?
                NotificationType.REVIEW.getDescription() : "리뷰 답글 알림";
    }

    public String toDisplayScheduleType(UserRole userRole, String content) {

        if (content.contains("취소")) return "예약 취소";
        if (content.contains("변경")) return "예약 변경";

        return NotificationType.RESERVATION.getDescription();
    }

    public String toDisplayReservationType(UserRole userRole, String content) {
        if (userRole == UserRole.MODEL){
            if (content.contains("확정")) return "예약 확정";
            if (content.contains("취소")) return "예약 취소";
            else new GeneralException(ErrorStatus._BAD_REQUEST);
        } else if (userRole == UserRole.DESIGNER){
            return "예약 신청 알림";
        }

        return NotificationType.RESERVATION.getDescription();
    }
}
