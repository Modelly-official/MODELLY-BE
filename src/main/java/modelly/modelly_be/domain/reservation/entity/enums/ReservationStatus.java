package modelly.modelly_be.domain.reservation.entity.enums;

public enum ReservationStatus {
    RESERVATION_CONFIRMED("예약확정"),
    RESERVATION_PENDING("예약대기"),
    RESERVATION_CANCELLED("예약취소");

    private final String description;

    ReservationStatus(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
