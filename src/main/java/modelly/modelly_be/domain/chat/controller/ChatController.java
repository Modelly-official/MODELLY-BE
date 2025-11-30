package modelly.modelly_be.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.dto.request.OpenRoomRequest;
import modelly.modelly_be.domain.chat.dto.request.SendMessageRequest;
import modelly.modelly_be.domain.chat.dto.response.*;
import modelly.modelly_be.domain.chat.entity.Chatting;
import modelly.modelly_be.domain.chat.service.ChatRoomService;
import modelly.modelly_be.domain.chat.service.ChattingService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "채팅 관련 API")
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

    @Operation(
            summary = "내 채팅방 목록 조회",
            description = "현재 로그인한 유저가 참여한 모든 채팅방 리스트를 조회합니다. 상대 정보(유저ID, 이름, 프로필 사진, 역할) + 마지막 메시지 정보를 반환합니다."
    )
    @GetMapping("/chat/rooms")
    public ApiResponse<List<ChatRoomListResponse>> getMyChatRoomList(
            @AuthenticationPrincipal AuthDetails currentUser
    ) {
        Long currentUserId = currentUser.user().getId();
        List<ChatRoomListResponse> rooms = chatRoomService.getMyChatRoomList(currentUserId);
        return ApiResponse.onSuccess(rooms);
    }

    @Operation(
            summary = "채팅 내역 조회",
            description = "특정 채팅방 정보(상대 유저)와 메시지 히스토리를 함께 반환합니다."
    )
    @GetMapping("/chat/rooms/{roomId}/messages")
    public ApiResponse<ChatRoomDetailResponse> getChatRoomDetail(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long roomId
    ) {
        Long currentUserId = auth.user().getId();
        ChatRoomDetailResponse response =
                chattingService.getChatRoomDetail(currentUserId, roomId);

        return ApiResponse.onSuccess(response);
    }
}

