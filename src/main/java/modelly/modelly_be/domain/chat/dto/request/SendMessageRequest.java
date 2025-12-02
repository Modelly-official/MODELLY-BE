package modelly.modelly_be.domain.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    private String message;

    public static SendMessageRequest of(String message){
        return new SendMessageRequest(message);
    }

}
