package modelly.modelly_be.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.controller.swagger.ChatSwagger;
import modelly.modelly_be.domain.chat.dto.request.OpenRoomRequest;
import modelly.modelly_be.domain.chat.dto.response.*;
import modelly.modelly_be.domain.chat.service.ChatRoomService;
import modelly.modelly_be.domain.chat.service.ChattingService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.s3.S3Uploader;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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

    @PostMapping("/chat/rooms/{roomId}/images")
    public ApiResponse<List<String>> uploadChatImages(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long roomId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        Long currentUserId = auth.user().getId();

        // 파일 개수 검증
        if (files == null || files.isEmpty()) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }
        if (files.size() > 10) {  // 최대 10개로 제한
            throw new GeneralException(ErrorStatus.TOO_MANY_FILES);
        }
        // 방에 유저가 참여 중인지 검증
        chatRoomService.validateParticipation(currentUserId, roomId);

        // 업로드 + 부분 실패 롤백
        List<String> urls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String url = s3Uploader.upload(file, "chat");
                urls.add(url);
            }
        } catch (Exception e) {
            // 이미 업로드된 파일들 정리
            urls.forEach(url -> {
                try {
                    s3Uploader.delete(url);
                } catch (Exception ignored) {
                }
            });
            throw e;
        }

        return ApiResponse.onSuccess(urls);
    }
}

