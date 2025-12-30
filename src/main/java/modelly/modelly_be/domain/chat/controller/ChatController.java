package modelly.modelly_be.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.chat.controller.swagger.ChatSwagger;
import modelly.modelly_be.domain.chat.dto.request.OpenRoomRequest;
import modelly.modelly_be.domain.chat.dto.response.*;
import modelly.modelly_be.domain.chat.service.ChatRoomService;
import modelly.modelly_be.domain.chat.service.ChattingService;
import modelly.modelly_be.domain.reservation.dto.internal.ChatRoomReservationSummary;
import modelly.modelly_be.domain.reservation.dto.response.ChatRoomReservationSummaryResponse;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ChatController implements ChatSwagger {

    private final ChatRoomService chatRoomService;
    private final ChattingService chattingService;
    private final ReservationService reservationService;

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

    // 해당 채팅방에 엮인 예약 조회
    @GetMapping("/chat/rooms/{roomId}/reservation")
    public ApiResponse<ChatRoomReservationSummaryResponse> getRoomReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long roomId
    ) {
        ChatRoomReservationSummary summary =
                reservationService.getChatRoomReservationSummary(roomId, auth.user());

        return ApiResponse.onSuccess(
                new ChatRoomReservationSummaryResponse(summary != null, summary)
        );
    }

}

