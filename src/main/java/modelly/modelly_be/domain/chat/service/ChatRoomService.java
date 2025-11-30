package modelly.modelly_be.domain.chat.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.response.ChatRoomListResponse;
import modelly.modelly_be.domain.chat.dto.response.OpenRoomResponse;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.repository.ChatRoomRepository;
import modelly.modelly_be.domain.chat.repository.ChattingRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.repository.DesignerRepository;
import modelly.modelly_be.domain.user.repository.ModelRepository;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

            model = modelRepository.findByUser_Id(target.getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));
        }
        // current = 모델, target = 디자이너
        else if (me.getUserRole() == UserRole.MODEL && target.getUserRole() == UserRole.DESIGNER) {
            designer = designerRepository.findByUser_Id(target.getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));
            model = modelRepository.findByUser_Id(me.getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));
        }
        else {
            throw new GeneralException(ErrorStatus.INVALID_CHATROOM);
        }

        // 디자이너-모델쌍 채팅방 반환, 존재하지 않으면 새로운 채팅방 생성
        ChatRoom chatRoom = chatRoomRepository.findByDesignerAndModel(designer, model)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.of(designer, model)));

        return OpenRoomResponse.of(chatRoom.getId());
    }

    /* 채팅방 리스트 조회 */
    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getMyChatRoomList(Long currentUserId) {

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

        // 현재 유저가 모델인 경우
        if (isModel && !isDesigner) {
            Long modelId = modelOpt.get().getId();
            rooms = chatRoomRepository.findAllByModelIdWithDesigner(modelId);
        }
        // 현재 유저가 디자이너인 경우
        else if (!isModel && isDesigner) {
            Long designerId = designerOpt.get().getId();
            rooms = chatRoomRepository.findAllByDesignerIdWithModel(designerId);
        }

        /* 응답 생성 */
        List<ChatRoomListResponse> result = new ArrayList<>();

        for (ChatRoom room : rooms) {

            Model model = room.getModel();
            Designer designer = room.getDesigner();

            boolean iAmModel = isModel && model != null
                    && model.getUser().getId().equals(currentUserId);

            Long opponentUserId;
            String opponentName;
            String opponentProfileImageUrl;
            UserRole opponentRole;

            // 현재 유저가 모델인 경우
            if (iAmModel) {
                User designerUser = designer.getUser();
                opponentUserId = designerUser.getId();
                opponentName = designer.getNickname();
                opponentProfileImageUrl = designerUser.getImageUrl();
                opponentRole = UserRole.DESIGNER;
            }
            // 현재 유저가 디자이너인 경우
            else {
                User modelUser = model.getUser();
                opponentUserId = modelUser.getId();
                opponentName = modelUser.getName();
                opponentProfileImageUrl = modelUser.getImageUrl();
                opponentRole = UserRole.MODEL;
            }

            Chatting last = chattingRepository.findTopByChatRoomOrderByCreatedAtDesc(room);

            result.add(ChatRoomListResponse.builder()
                    .roomId(room.getId())
                    .userId(opponentUserId)
                    .name(opponentName)
                    .profileImageUrl(opponentProfileImageUrl)
                    .lastMessage(last != null ? last.getMessage() : null)
                    .lastMessageTime(last != null ? last.getCreatedAt() : null)
                    .role(opponentRole)
                    .build());
        }

        // 마지막 메세지 시간 기준으로 채팅방 응답 정렬(최신이 위로)
        result.sort((a, b) -> {
            var t1 = a.getLastMessageTime();
            var t2 = b.getLastMessageTime();

            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;

            return t2.compareTo(t1); // 내림차순
        });
        return result;
    }
}
