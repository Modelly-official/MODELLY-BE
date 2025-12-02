package modelly.modelly_be.domain.chat.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.response.ChatRoomListResponse;
import modelly.modelly_be.domain.chat.dto.response.OpenRoomResponse;
import modelly.modelly_be.domain.chat.dto.response.OpponentInfoResponse;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.repository.ChatRoomRepository;
import modelly.modelly_be.domain.chat.repository.ChattingRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.repository.designerRepository.DesignerRepository;
import modelly.modelly_be.domain.user.repository.ModelRepository;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final DesignerRepository designerRepository;
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;
    private final ChattingRepository chattingRepository;

    /* 채팅방 생성 및 채팅방 ID 반환 */
    @Transactional
    public OpenRoomResponse openRoom(Long currentUserId, Long targetUserId) {
        User me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        Designer designer;
        Model model;

        // current = 디자이너, target = 모델
        if (me.getUserRole() == UserRole.DESIGNER && target.getUserRole() == UserRole.MODEL) {

            designer = designerRepository.findByUser_Id(me.getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_DESIGNER));

            model = modelRepository.findByUser_Id(target.getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_MODEL));
        }
        // current = 모델, target = 디자이너
        else if (me.getUserRole() == UserRole.MODEL && target.getUserRole() == UserRole.DESIGNER) {
            designer = designerRepository.findByUser_Id(target.getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_DESIGNER));
            model = modelRepository.findByUser_Id(me.getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_MODEL));
        }
        else {
            throw new GeneralException(ErrorStatus.INVALID_CHATROOM);
        }

        // 디자이너-모델쌍 채팅방 반환, 존재하지 않으면 새로운 채팅방 생성
        ChatRoom chatRoom = chatRoomRepository.findByDesignerAndModel(designer, model)
                .orElseGet(() -> createChatRoomSafely(designer, model));

        return OpenRoomResponse.of(chatRoom.getId());
    }

    /* 동시에 채팅방 생성 요청 들어온 경우를 처리하는 핸들러 */
    private ChatRoom createChatRoomSafely(Designer designer, Model model) {
        try {
            return chatRoomRepository.save(ChatRoom.of(designer, model));
        } catch (DataIntegrityViolationException e) {
            // 이미 다른 트랜잭션이 먼저 채팅방을 생성한 경우, 이미 만들어진 채팅방 반환
            return chatRoomRepository.findByDesignerAndModel(designer, model)
                    .orElseThrow(() -> new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR));
        }
    }

    /* 채팅방 리스트 조회 */
    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getMyChatRoomList(Long currentUserId, int page, int size) {

        var modelOpt = modelRepository.findByUser_Id(currentUserId);
        var designerOpt = designerRepository.findByUser_Id(currentUserId);

        boolean isModel = modelOpt.isPresent();
        boolean isDesigner = designerOpt.isPresent();

        if (!isModel && !isDesigner) {
            throw new GeneralException(ErrorStatus.NOT_FOUND_USER);
        }

        if (isModel && isDesigner) {
            throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
        }

        /* 채팅방 수집 */
        List<ChatRoom> rooms = new ArrayList<>();

        Pageable pageable = PageRequest.of(page, size);

        // 현재 유저가 모델인 경우
        if (isModel) {
            Long modelId = modelOpt.get().getId();
            rooms = chatRoomRepository
                    .findAllByModelIdOrderByLastMessageTimeDesc(modelId, pageable) // 페이지 단위로 채팅방 수집
                    .getContent();
        }
        // 현재 유저가 디자이너인 경우
        else if (isDesigner) {
            Long designerId = designerOpt.get().getId();
            rooms = chatRoomRepository
                    .findAllByDesignerIdOrderByLastMessageTimeDesc(designerId, pageable) // 페이지 단위로 채팅방 수집
                    .getContent();
        }

        if (rooms.isEmpty()) {
            return List.of();
        }

        // 모든 채팅방에 대한 마지막 메시지를 한 번에 조회
        List<Chatting> lastMessages = chattingRepository.findLastMessagesByChatRooms(rooms);

        // 채팅방 ID와 Chatting 매핑
        Map<Long, Chatting> lastMessageMap = lastMessages.stream()
                .collect(Collectors.toMap(
                        c -> c.getChatRoom().getId(),
                        Function.identity()
                ));

        /* 응답 생성 */
        List<ChatRoomListResponse> result = new ArrayList<>();

        for (ChatRoom room : rooms) {
            OpponentInfoResponse opponent = getOpponentInfo(room, currentUserId);

            Chatting last = lastMessageMap.get(room.getId());

            result.add(ChatRoomListResponse.builder()
                    .roomId(room.getId())
                    .otherUserId(opponent.getUserId())
                    .name(opponent.getName())
                    .profileImageUrl(opponent.getProfileImageUrl())
                    .messageType(last != null ? last.getMessageType() : null)
                    .lastMessage(last != null ? last.getMessage() : null)
                    .lastMessageTime(last != null ? last.getCreatedAt() : null)
                    .role(opponent.getRole())
                    .build());
        }

        return result;
    }

    public OpponentInfoResponse getOpponentInfo(ChatRoom room, Long currentUserId) {
        Model model = room.getModel();
        Designer designer = room.getDesigner();

        boolean iAmModel = model != null
                && model.getUser().getId().equals(currentUserId);

        Long opponentUserId;
        String opponentName;
        String opponentProfileImageUrl;
        UserRole opponentRole;

        // 현재 유저가 모델인 경우 → 상대는 디자이너
        if (iAmModel) {
            User designerUser = designer.getUser();
            opponentUserId = designerUser.getId();
            opponentName = designer.getNickname();          // 활동명
            opponentProfileImageUrl = designerUser.getImageUrl();
            opponentRole = UserRole.DESIGNER;
        }
        // 현재 유저가 디자이너인 경우 → 상대는 모델
        else {
            User modelUser = model.getUser();
            opponentUserId = modelUser.getId();
            opponentName = modelUser.getName();             // 이름
            opponentProfileImageUrl = modelUser.getImageUrl();
            opponentRole = UserRole.MODEL;
        }

        return OpponentInfoResponse.builder()
                .userId(opponentUserId)
                .name(opponentName)
                .profileImageUrl(opponentProfileImageUrl)
                .role(opponentRole)
                .build();
    }

    @Transactional(readOnly = true)
    public void validateParticipation(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_CHAT_ROOM));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        if (!room.isParticipant(user)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
    }
}
