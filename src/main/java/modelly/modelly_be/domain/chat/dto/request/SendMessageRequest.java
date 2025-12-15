package modelly.modelly_be.domain.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    private String message;                  // TEXT일 때만 사용
    private List<String> imageUrls;          // IMAGE일 때 사용
    private MessageType messageType;         // TEXT or IMAGE

    public static SendMessageRequest text(String message) {
        SendMessageRequest req = new SendMessageRequest();
        req.messageType = MessageType.TEXT;
        req.message = message;
        return req;
    }

    public static SendMessageRequest image(List<String> imageUrls) {
        SendMessageRequest req = new SendMessageRequest();
        req.messageType = MessageType.IMAGE;
        req.imageUrls = imageUrls;
        return req;
    }

}
