package modelly.modelly_be.global.entity;

public enum Category {
    HAIR("헤어"),
    NAIL("네일"),
    TATTOO("타투"),
    EYELASH("속눈썹"),
    ETC("기타")
    ;

    private final String description;

    Category(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
