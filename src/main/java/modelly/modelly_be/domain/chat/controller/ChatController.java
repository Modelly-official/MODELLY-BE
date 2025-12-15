package modelly.modelly_be.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.controller.swagger.ChatSwagger;
import modelly.modelly_be.domain.chat.dto.request.OpenRoomRequest;
import modelly.modelly_be.domain.chat.dto.response.*;
import modelly.modelly_be.domain.chat.service.ChatRoomService;
import modelly.modelly_be.domain.chat.service.ChattingService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.s3.PresignedUploadResponse;
import modelly.modelly_be.global.s3.S3Uploader;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController implements ChatSwagger {

    private final ChatRoomService chatRoomService;
    private final ChattingService chattingService;
    private final S3Uploader s3Uploader;

    @PostMapping("/chat/rooms")
    public ApiResponse<OpenRoomResponse> openRoom(
            @AuthenticationPrincipal AuthDetails currentUser,
            @RequestBody OpenRoomRequest request) {

        Long currentUserId = currentUser.user().getId();
        Long targetUserId = request.getTargetUserId();

        return ApiResponse.onSuccess(chatRoomService.openRoom(currentUserId, targetUserId));
    }

    @GetMapping("/chat/rooms")
    public ApiResponse<List<ChatRoomListResponse>> getMyChatRoomList(
            @AuthenticationPrincipal AuthDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long currentUserId = currentUser.user().getId();
        List<ChatRoomListResponse> rooms = chatRoomService.getMyChatRoomList(currentUserId, page, size);
        return ApiResponse.onSuccess(rooms);
    }

    @GetMapping("/chat/rooms/{roomId}/messages")
    public ApiResponse<ChatRoomDetailResponse> getChatRoomDetail(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursorMessageId,
            @RequestParam(defaultValue = "20") int size            // 한 번에 가져올 개수
    ) {
        Long currentUserId = auth.user().getId();
        ChatRoomDetailResponse response =
                chattingService.getChatRoomDetail(currentUserId, roomId, cursorMessageId, size);

        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/chat/rooms/{roomId}/images/presigned")
    public ApiResponse<PresignedUploadResponse> createPresignedUrl(
            @PathVariable Long roomId,
            @AuthenticationPrincipal AuthDetails auth
    ) {
        chatRoomService.validateParticipation(auth.user().getId(), roomId);

        PresignedUploadResponse response =
                s3Uploader.generatePresignedUrl("chat/" + roomId);

        return ApiResponse.onSuccess(response);
    }
}

