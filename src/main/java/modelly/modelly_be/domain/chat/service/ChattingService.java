package modelly.modelly_be.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.dto.response.ChatMessageResponse;
import modelly.modelly_be.domain.chat.dto.response.ChatRoomDetailResponse;
import modelly.modelly_be.domain.chat.dto.response.OpponentInfoResponse;
import modelly.modelly_be.domain.chat.dto.response.SendMessageResponse;
import modelly.modelly_be.domain.chat.entity.ChatRoom;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.entity.ChattingImage;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;
import modelly.modelly_be.domain.chat.repository.ChatRoomRepository;
import modelly.modelly_be.domain.chat.repository.ChattingImageRepository;
import modelly.modelly_be.domain.chat.repository.ChattingRepository;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.repository.UserRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChattingService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChattingRepository chattingRepository;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final ChattingImageRepository chattingImageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // 메세지 보내기(DB 저장)
    @Transactional
    public SendMessageResponse sendMessage(Long currentUserId, Long roomId, SendMessageRequest request) {

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

        // 메세지 타입 확인
        MessageType type = request.getMessageType();
        if (type == null) {
            type = MessageType.TEXT;
        }


        String messageContent = null;
        if (type == MessageType.TEXT) {
            messageContent = request.getMessage();
            if (messageContent == null || messageContent.isBlank()) {
                throw new GeneralException(ErrorStatus._BAD_REQUEST);
            }
        } else if (type == MessageType.IMAGE) {
            if (request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
                throw new GeneralException(ErrorStatus._BAD_REQUEST);
            }

            boolean hasInvalidUrl = request.getImageUrls().stream()
                    .anyMatch(url -> url == null || url.isBlank());
            if (hasInvalidUrl) {
                throw new GeneralException(ErrorStatus._BAD_REQUEST);
            }

        } else {
            // 지원하지 않는 타입 방어
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        // Chatting 저장
        Chatting chatting = Chatting.of(
                sender.getId(),
                messageContent,
                false,
                type,
                room
        );
        Chatting saved = chattingRepository.save(chatting);

        // IMAGE면 chatting_image 저장
        List<ChattingImage> images = List.of();
        if (type == MessageType.IMAGE) {
            images = request.getImageUrls().stream()
                    .map(url -> ChattingImage.of(saved, url))
                    .toList();
            chattingImageRepository.saveAll(images);
        }

        // STOMP 응답 DTO 반환
        return SendMessageResponse.of(saved, images);
    }

    /* 채팅방 상세 조회 (상대 정보, 채팅 내역 등) */
    @Transactional
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

        // 처음 채팅방 진입 시
        if (cursorMessageId == null || cursorMessageId == 0) {
            chattingRepository.markUnreadMessagesAsReadInRoom(room, currentUserId);
        }

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

        // 이미지 조회 후 매핑
        if (!messages.isEmpty()) {
            // 해당 메시지들에 달린 모든 이미지 한 번에 조회
            List<ChattingImage> allImages =
                    chattingImageRepository.findAllByChattingIn(messages);

            // chattingId 기준으로 그룹핑 (1:N)
            Map<Long, List<String>> imageUrlMap = allImages.stream()
                    .collect(Collectors.groupingBy(
                            ci -> ci.getChatting().getId(),
                            Collectors.mapping(ChattingImage::getImageUrl, Collectors.toList())
                    ));

            // 각 메시지에 해당하는 imageUrls 찾아서 DTO로 변환
            for (Chatting chatting : messages) {
                List<String> imageUrls =
                        imageUrlMap.getOrDefault(chatting.getId(), List.of());
                messageResponses.add(ChatMessageResponse.of(chatting, imageUrls));
            }
        }

        // 다음 커서 (이번에 받은 것 중 "가장 오래된" 메시지의 id)
        Long nextCursor = messages.isEmpty() ? null : messages.get(0).getId();

        boolean hasNext = messages.size() == size; // 메세지와 반환 메세지 수가 다르다면 더이상 반환할 메세지가 없음

        Long lastReadMessageId = chattingRepository.findLastReadMessageIdByRoomAndReader(room, currentUserId);
        /* 전체 응답 반환 */
        return ChatRoomDetailResponse.builder()
                .roomId(room.getId())
                .opponent(opponent)
                .messages(messageResponses)
                .nextCursorMessageId(nextCursor)
                .hasNext(hasNext)
                .lastReadMessageId(lastReadMessageId)
                .build();
    }


    // 예약 관련 메세지 브로드캐스트용
    @Transactional
    public SendMessageResponse publishReservationMessage(Long senderUserId, Long roomId, String message) {
        // sender 검증
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        // 채팅방 검증
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_CHAT_ROOM));

        // 참여자인지 검증
        if (!room.isParticipant(sender)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        // 메세지 저장
        Chatting saved = chattingRepository.save(
                Chatting.of(
                        sender.getId(),
                        message,
                        false,
                        MessageType.RESERVATION,
                        room
                )
        );

        SendMessageResponse response = SendMessageResponse.of(saved, List.of());

        // 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + roomId, response);

        return response;
    }

    // RESERVATION 메시지: payload(Object) -> JSON 문자열로 저장 + 브로드캐스트
    @Transactional
    public SendMessageResponse publishReservationPayload(Long senderUserId, Long roomId, Object payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
        return publishReservationMessage(senderUserId, roomId, json);
    }

    // TEXT 메시지: 텍스트 저장 + 브로드캐스트
    @Transactional
    public SendMessageResponse publishTextMessage(Long senderUserId, Long roomId, String text) {

        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_CHAT_ROOM));

        if (!room.isParticipant(sender)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        if (text == null || text.isBlank()) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        Chatting saved = chattingRepository.save(
                Chatting.of(
                        sender.getId(),
                        text,
                        false,
                        MessageType.TEXT,
                        room
                )
        );

        SendMessageResponse response = SendMessageResponse.of(saved, List.of());
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + roomId, response);

        return response;
    }
}
