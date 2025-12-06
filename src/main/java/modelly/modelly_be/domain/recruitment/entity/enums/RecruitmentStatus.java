package modelly.modelly_be.domain.recruitment.entity.enums;

public enum RecruitmentStatus {
    OPEN("모집중"),
    CLOSED("모집마감");

    private final String description;

    RecruitmentStatus(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
