package modelly.modelly_be.domain.recruitment.entity;

public enum Category {
    HAIR("헤어"),
    NAIL("네일"),
    TATTOO("타투"),
    SHOOTING("촬영"),
    MAKEUP("메이크업"),
    ETC("기타")
    ;

    private final String description;

    Category(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
