package modelly.modelly_be.domain.chat.entity.enums;

public enum MessageType {
    TEXT("텍스트"),
    IMAGE("이미지")
    ;

    private final String description;

    MessageType(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
