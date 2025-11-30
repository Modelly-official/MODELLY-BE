package modelly.modelly_be.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import modelly.modelly_be.domain.user.entity.enums.UserRole;

import java.util.List;

@Getter
@Builder
public class ChatRoomDetailResponse {
    private Long roomId;

    // 상대 유저 정보
    private OpponentInfoResponse opponent;

    // 메시지 리스트
    private List<ChatMessageResponse> messages;

    // 예약 정보(예약 도메인 기능 개발 이후 추가)
}
