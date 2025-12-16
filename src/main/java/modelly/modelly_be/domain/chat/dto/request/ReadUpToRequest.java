package modelly.modelly_be.domain.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadUpToRequest {
    private Long lastMessageId; // 화면에서 실제로 본 마지막 메시지 ID
}
