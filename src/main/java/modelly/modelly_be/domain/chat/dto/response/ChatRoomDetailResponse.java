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

    //무한 스크롤용 추가 필드
    private Long nextCursorMessageId;  // 다음 요청 시 cursorMessageId로 사용할 값 (가장 오래된 메시지의 id)
    private boolean hasNext;           // 더 불러올 메시지가 있는지 여부
}
