package modelly.modelly_be.domain.chat.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.repository.ChatRoomRepository;
import modelly.modelly_be.domain.chat.repository.ChattingRepository;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatReadService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChattingRepository chattingRepository;

    @Transactional
    public Long readMessagesUpToId(Long currentUserId, Long roomId, Long lastMessageId) {
        User me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_CHAT_ROOM));

        if (!room.isParticipant(me)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        if (lastMessageId == null) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        // id <= lastMessageId 인 상대 메세지들을 한 번에 읽음 처리
        chattingRepository.readMessagesUpToId(room, currentUserId, lastMessageId);

        // 내가 읽은 상대 메세지들 중 가장 마지막 ID 리턴
        return chattingRepository.findLastReadMessageIdByRoomAndReader(room, currentUserId);
    }

    /* 전체 안읽은 메세지 수 조회 - 홈화면 채팅탭에 사용 */
    @Transactional(readOnly = true)
    public long getTotalUnreadCount(Long currentUserId) {
        return chattingRepository.countTotalUnreadByUser(currentUserId);
    }
}
