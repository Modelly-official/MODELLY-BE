package modelly.modelly_be.domain.chat.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.response.OpenRoomResponse;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.repository.ChatRoomRepository;
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

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final DesignerRepository designerRepository;
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;

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
}
