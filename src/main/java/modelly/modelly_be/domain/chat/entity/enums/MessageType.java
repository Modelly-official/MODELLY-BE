package modelly.modelly_be.domain.chat.entity.enums;

public enum MessageType {
    TEXT("텍스트"),
<<<<<<< HEAD
    IMAGE("이미지"),
    READ("읽음 여부")
=======
    IMAGE("이미지")
>>>>>>> 7b5ac96 (feat: 채팅 이미지 S3 업로드 및 이미지 전송 처리 (#21))
    ;

    private final String description;

    MessageType(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
