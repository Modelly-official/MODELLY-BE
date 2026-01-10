package modelly.modelly_be.global.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    HAIR("헤어"),
    NAIL("네일"),
    TATTOO("타투"),
    EYELASH("속눈썹")
    ;

    private final String description;

    Category(String description) {this.description = description;}

    @JsonValue
    public String getDescription() {
        return description;
    }
}
