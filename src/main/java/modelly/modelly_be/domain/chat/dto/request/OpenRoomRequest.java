package modelly.modelly_be.domain.chat.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class OpenRoomRequest {
    private Long targetUserId;

    public static OpenRoomRequest of(Long targetUserId) {
        return new OpenRoomRequest(targetUserId);
    }
}
