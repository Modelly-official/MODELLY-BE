package modelly.modelly_be.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.request.ReadUpToRequest;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.dto.response.ReadNotificationResponse;
import modelly.modelly_be.domain.chat.dto.response.SendMessageResponse;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.entity.enums.MessageType;
import modelly.modelly_be.domain.chat.service.ChatReadService;
import modelly.modelly_be.domain.chat.service.ChattingService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChattingService chattingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatReadService chatReadService;

    @MessageMapping("/chat/rooms/{roomId}")
    public void handleMessage(
            Authentication authentication,
            @DestinationVariable Long roomId,
            SendMessageRequest request
    ) {

        AuthDetails auth = (AuthDetails) authentication.getPrincipal();
        Long currentUserId = auth.user().getId();

        SendMessageResponse response = chattingService.sendMessage(
                currentUserId,
                roomId,
                request
        );

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId,
                response
        );

    }

    // 메세지 읽음(브로드캐스팅)
    @MessageMapping("/chat/rooms/{roomId}/read")
    public void handleRead(
            Authentication authentication,
            @DestinationVariable Long roomId,
            ReadUpToRequest request
    ) {
        AuthDetails auth = (AuthDetails) authentication.getPrincipal();
        Long currentUserId = auth.user().getId();

        // LastMessageId까지 읽음 상태로 변경
        Long lastReadMessageId =
                chatReadService.readMessagesUpToId(currentUserId, roomId, request.getLastMessageId());

        // 읽은 메시지가 없으면 이벤트 보내지 않음
        if (lastReadMessageId == null) {
            return;
        }

        // 상대에게 읽음 이벤트 브로드캐스트
        ReadNotificationResponse payload =
                new ReadNotificationResponse(roomId, MessageType.READ, currentUserId, lastReadMessageId);

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId,
                payload
        );
    }
}
