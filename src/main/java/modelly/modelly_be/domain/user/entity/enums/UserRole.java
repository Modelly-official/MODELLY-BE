package modelly.modelly_be.domain.user.entity.enums;

public enum UserRole {
    DESIGNER("디자이너"),
    MODEL("모델")
    ;

    private final String description;

    UserRole(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
