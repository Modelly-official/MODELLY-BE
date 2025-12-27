package modelly.modelly_be.domain.reservation.entity.enums;

public enum ReservationChangeStatus {
    PENDING("변경 요청 대기"),
    ACCEPTED("변경 수락"),
    REJECTED("변경 거절"),
    CANCELED("요청 취소");

    private final String description;

    ReservationChangeStatus(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
