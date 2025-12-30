package modelly.modelly_be.domain.reservation.entity.enums;

public enum ReservationListType {
    UPCOMING("다가오는 일정"),
    COMPLETED("완료된 일정");

    private final String description;

    ReservationListType(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
