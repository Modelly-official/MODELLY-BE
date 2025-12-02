package modelly.modelly_be.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.chat.entity.ChatRoom;

@Getter
@AllArgsConstructor
public class OpenRoomResponse {
    private Long chatRoomId;

    public static OpenRoomResponse of(Long chatRoomId) {
        return new OpenRoomResponse(chatRoomId);
    }
}
