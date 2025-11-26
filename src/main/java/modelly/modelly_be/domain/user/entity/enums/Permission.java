package modelly.modelly_be.domain.user.entity.enums;

public enum Permission {
    USER("유저"),
    ADMIN("어드민");

    private final String description;

    Permission(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
