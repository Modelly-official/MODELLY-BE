package modelly.modelly_be.domain.notification.entity;

public enum NotificationType {
    RESERVATION("예약"),
    CHATTING("채팅"),
    REVIEW("리뷰"),
    SCHEDULE("일정");

    private final String description;

    NotificationType(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
