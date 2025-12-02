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
            summary = "내 채팅방 목록 조회 (페이지네이션 / 무한 스크롤)",
            description = """
                현재 로그인한 유저가 참여한 채팅방 목록을 조회합니다.
                
                정렬 기준
                - 각 채팅방의 '마지막 메시지 시간'을 기준으로 내림차순 정렬됩니다.
                - 아직 메시지가 한 번도 없는 채팅방은 채팅방 생성 시간(createdAt)을 기준으로 정렬됩니다.
                - 따라서 가장 최근에 대화한 채팅방이 리스트의 맨 위에 오도록 정렬됩니다.
                
                페이징 방식 (page / size)
                - page: 0부터 시작하는 페이지 번호입니다.
                  - page=0, size=20 → 가장 최근 채팅방 20개
                  - page=1, size=20 → 그 다음 채팅방 20개
                - size: 한 번에 가져올 채팅방 개수입니다. (기본값 20)
                - 무한 스크롤을 구현 시 처음에는 page=0으로 호출하고 스크롤이 끝에 닿을 때마다 page를 1, 2, 3 식으로 증가시키며 재호출하면 됩니다.
                """
    )
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

    @Operation(
            summary = "채팅 내역 조회 (cursor 기반 무한 스크롤)",
            description = """
                특정 채팅방의 상대 정보와 메시지 히스토리를 부분적으로 조회합니다.
                메시지는 "과거 -> 최신" 시간 오름차순으로 반환되며, 위로 스크롤하는 방식의 무한 스크롤에 맞춰져 있습니다.
                
                최초 호출 시 (가장 최근 메시지 불러오기)
                - cursorMessageId를 보내지 않고 호출합니다. (cursorMessageId = null)
                  - 예시: GET /chat/rooms/{roomId}/messages?size=20
                - 해당 채팅방의 가장 최근 메시지부터 최대 size개까지 반환합니다.
                - 응답에 포함된 messages 배열의 맨 아래가 가장 최신 메시지가 되도록 정렬됩니다.
                
                과거 메시지 더 불러오기 (위로 스크롤)
                - 프론트에서 현재 화면에 로드된 메시지들 중 '가장 위에 있는(가장 오래된) 메시지의 messageId'를 cursorMessageId로 보내면,
                  서버는 해당 ID보다 더 이전(id < cursorMessageId) 메시지를 size개 내려줍니다.
                  - 예시: GET /chat/rooms/{roomId}/messages?cursorMessageId=40&size=20
                  
                응답
                - messages: 시간 기준 오름차순 (오래된 메시지 -> 최신 메시지)
                - nextCursorMessageId:
                  - 이번에 내려준 메시지들 중 가장 오래된 메시지의 ID
                  - 다음 요청 시 cursorMessageId로 사용하면 됩니다.
                - hasNext:
                  - true: 아직 더 이전 메시지가 남아 있음 (계속 스크롤 가능)
                  - false: 더 이상 불러올 이전 메시지가 없음 (최초 메시지까지 도달)
                """
    )
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
}

