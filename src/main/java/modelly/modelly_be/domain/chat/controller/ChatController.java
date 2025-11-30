package modelly.modelly_be.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.request.OpenRoomRequest;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.dto.response.OpenRoomResponse;
import modelly.modelly_be.domain.chat.dto.response.SendMessageResponse;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.service.ChatRoomService;
import modelly.modelly_be.domain.chat.service.ChattingService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChattingService chattingService;

    @Operation(
            summary = "채팅방 생성 또는 기존 채팅방 조회",
            description = "현재 로그인한 유저와 targetUserId(상대방) 조합으로 채팅방을 생성하거나, 이미 존재하면 해당 채팅방을 반환합니다."
    )
    @PostMapping("/chat/rooms")
    public ApiResponse<OpenRoomResponse> openRoom(
            @AuthenticationPrincipal AuthDetails currentUser,
            @RequestBody OpenRoomRequest request) {

        Long currentUserId = currentUser.user().getId();
        Long targetUserId = request.getTargetUserId();

        return ApiResponse.onSuccess(chatRoomService.openRoom(currentUserId, targetUserId));
    }

}

