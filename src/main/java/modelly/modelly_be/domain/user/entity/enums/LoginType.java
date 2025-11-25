package modelly.modelly_be.domain.user.entity.enums;

public enum LoginType {
    JWT("일반"),
    KAKAO("카카오"),
    NAVER("네이버"),
    GOOGLE("구글");

    private final String description;

    LoginType(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
