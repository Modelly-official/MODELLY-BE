package modelly.modelly_be.domain.chat.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.dto.response.ChatMessageResponse;
import modelly.modelly_be.domain.chat.dto.response.ChatRoomDetailResponse;
import modelly.modelly_be.domain.chat.dto.response.OpponentInfoResponse;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.repository.ChatRoomRepository;
import modelly.modelly_be.domain.chat.repository.ChattingRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChattingService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChattingRepository chattingRepository;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;

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

    /* 채팅방 상세 조회 (상대 정보, 채팅 내역 등) */
    @Transactional(readOnly = true)
    public ChatRoomDetailResponse getChatRoomDetail(
            Long currentUserId,
            Long roomId,
            Long cursorMessageId,   // null이면 최신부터
            int size
    ) {

        // 유저 검증
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        // 채팅방 조회
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_CHAT_ROOM));

        // 참여자 검증
        if (!room.isParticipant(currentUser)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        OpponentInfoResponse opponent = chatRoomService.getOpponentInfo(room, currentUserId);

        /* --- 메세지 히스토리 관련 코드 ---- */

        Pageable pageable = PageRequest.of(0, size);

        List<Chatting> messages;

        if (cursorMessageId == null) {
            // 최초 요청: 최신 메시지부터 size개
            messages = chattingRepository.findByChatRoomOrderByIdDesc(room, pageable);
        } else {
            // 이후 요청: cursorMessageId 이전 메시지들 size개
            messages = chattingRepository.findByChatRoomAndIdLessThanOrderByIdDesc(
                    room, cursorMessageId, pageable
            );
        }

        // desc로 가져온 걸 asc로 정렬해서 프론트에서 위에 쌓기 좋게(오래된 메세지 -> 최신 메세지 순)
        messages.sort(Comparator.comparingLong(Chatting::getId));

        // 응답 변환
        List<ChatMessageResponse> messageResponses = new ArrayList<>();
        for (Chatting chatting : messages) {
            messageResponses.add(ChatMessageResponse.of(chatting));
        }

        // 다음 커서 (이번에 받은 것 중 "가장 오래된" 메시지의 id)
        Long nextCursor = messages.isEmpty() ? null : messages.get(0).getId();

        boolean hasNext = messages.size() == size; // 메세지와 반환 메세지 수가 다르다면 더이상 반환할 메세지가 없음

        /* 전체 응답 반환 */
        return ChatRoomDetailResponse.builder()
                .roomId(room.getId())
                .opponent(opponent)
                .messages(messageResponses)
                .nextCursorMessageId(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}
