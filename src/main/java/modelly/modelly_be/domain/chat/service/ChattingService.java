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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    /* 채팅방 상세 조회 (상대 정보, 채팅 내역 등) */
    @Transactional(readOnly = true)
    public ChatRoomDetailResponse getChatRoomDetail(Long currentUserId, Long roomId) {

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

        // 현재 유저가 모델인지 디자이너인지 구분
        Model model = room.getModel();
        Designer designer = room.getDesigner();

        boolean iAmModel = model != null
                && model.getUser().getId().equals(currentUserId);

        /* --- 상대 정보 저장 관련 코드 ---- */
        Long opponentUserId;
        String opponentName;
        String opponentProfileImageUrl;
        UserRole opponentRole;

        // 현재 유저가 모델인 경우
        if (iAmModel) {
            User designerUser = designer.getUser();
            opponentUserId = designerUser.getId();
            opponentName = designer.getNickname();          // 활동명
            opponentProfileImageUrl = designerUser.getImageUrl();
            opponentRole = UserRole.DESIGNER;
        }
        // 현재 유저가 디자이너인 경우
        else {
            User modelUser = model.getUser();
            opponentUserId = modelUser.getId();
            opponentName = modelUser.getName();             // 이름
            opponentProfileImageUrl = modelUser.getImageUrl();
            opponentRole = UserRole.MODEL;
        }

        OpponentInfoResponse opponent = OpponentInfoResponse.builder()
                .userId(opponentUserId)
                .name(opponentName)
                .profileImageUrl(opponentProfileImageUrl)
                .role(opponentRole)
                .build();

        /* --- 메세지 히스토리 관련 코드 ---- */

        // 해당 방의 모든 메시지를 오래된 순으로 조회
        List<Chatting> messages = chattingRepository.findByChatRoomOrderByCreatedAtAsc(room);

        // 메세지 응답 (isMine 계산 포함)
        List<ChatMessageResponse> messageResponses = new ArrayList<>();
        for (Chatting chatting : messages) {
            messageResponses.add(ChatMessageResponse.of(chatting));
        }

        /* 전체 응답 반환 */
        return ChatRoomDetailResponse.builder()
                .roomId(room.getId())
                .opponent(opponent)
                .messages(messageResponses)
                .build();
    }
}
