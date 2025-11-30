package modelly.modelly_be.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.dto.response.SendMessageResponse;
import modelly.modelly_be.domain.chat.entity.Chatting;
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

    @MessageMapping("/chat/rooms/{roomId}")
    public void handleMessage(
            Authentication authentication,
            @DestinationVariable Long roomId,
            SendMessageRequest request
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }

        AuthDetails auth = (AuthDetails) authentication.getPrincipal();
        Long currentUserId = auth.user().getId();

        Chatting saved = chattingService.sendMessage(
                currentUserId,
                roomId,
                request
        );

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + roomId,
                SendMessageResponse.from(saved)
        );

    }
}
