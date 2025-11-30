package modelly.modelly_be.domain.chat.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
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
public class ChattingService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChattingRepository chattingRepository;
    private final UserRepository userRepository;

    // 메세지 보내기(DB 저장)
    @Transactional
    public Chatting sendMessage(Long currentUserId, Long roomId, SendMessageRequest request) {

        // 보낸 사람 = 현재 유저
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        // 방 조회
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_CHAT_ROOM));

        // 참여자 검증
        if (!room.isParticipant(sender)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        // 메시지 생성
        Chatting chatting = Chatting.of(
                sender.getId(),
                request.getMessage(),
                false,
                room
        );

        return chattingRepository.save(chatting);
    }
}
