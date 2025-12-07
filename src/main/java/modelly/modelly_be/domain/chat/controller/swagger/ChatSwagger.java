package modelly.modelly_be.domain.chat.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.chat.dto.request.OpenRoomRequest;
import modelly.modelly_be.domain.chat.dto.response.ChatRoomDetailResponse;
import modelly.modelly_be.domain.chat.dto.response.ChatRoomListResponse;
import modelly.modelly_be.domain.chat.dto.response.OpenRoomResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(
        name = "채팅 관련 API",
        description = """
                채팅방 생성/조회, 채팅방 목록 조회, 채팅방 내역 조회, 채팅 이미지 업로드
                """
)
public interface ChatSwagger {

    @Operation(
            summary = "채팅방 생성 또는 기존 채팅방 조회",
            description = """
                    현재 로그인한 유저와 `targetUserId`(상대방 유저) 조합으로 1:1 채팅방을 생성하거나,  
                    이미 존재하는 경우 **기존 채팅방 ID를 그대로 반환**합니다.
                    
                    ✅ 디자이너 ↔ 모델 조합만 채팅 가능  
                    - currentUser = 디자이너, targetUser = 모델  
                    - currentUser = 모델, targetUser = 디자이너  
                    - 그 외 조합(모델-모델, 디자이너-디자이너)은 허용되지 않으며 에러가 발생합니다.
                    
                    ---
                    📥 Request Body
                    - `targetUserId` : 채팅을 시작할 상대방의 **User ID**
                    
                    📤 Response
                    - `chatRoomId` : 생성되었거나, 이미 존재하는 채팅방의 ID
                    
                    ⚠️ 동시 요청 처리
                    - 동일한 디자이너-모델 조합으로 채팅방 생성 요청이 동시에 들어와도  
                      DB unique 제약 + 예외 처리로 인해 **채팅방은 항상 1개만 유지**됩니다.
                    """
    )
    ApiResponse<OpenRoomResponse> openRoom(
            @AuthenticationPrincipal AuthDetails currentUser,
            @RequestBody OpenRoomRequest request
    );

    @Operation(
            summary = "내 채팅방 목록 조회 (페이지네이션 / 무한 스크롤)",
            description = """
                    현재 로그인한 유저가 참여 중인 채팅방 목록을 조회합니다.  
                    각 채팅방은 **가장 최근 메시지 시간**을 기준으로 정렬됩니다.
                    
                    ---
                    📌 정렬 기준
                    - 각 채팅방의 **마지막 메시지 생성 시간**을 기준으로 내림차순 정렬
                    - 아직 메시지가 한 번도 없는 채팅방은 `createdAt`(채팅방 생성 시간)을 기준으로 정렬
                    - 가장 최근에 대화한 채팅방이 리스트의 **맨 위**에 오도록 정렬됩니다.
                    
                    ---
                    📥 Request Param
                    
                    - `page` : 0부터 시작하는 페이지 번호  
                      - 예) `page=0, size=20` → 가장 최근 채팅방 20개  
                      - 예) `page=1, size=20` → 그 다음 채팅방 20개
                      
                    - `size` : 한 번에 가져올 채팅방 개수 (기본값 20)
                    
                    ---
                    📤 Response (ChatRoomListResponse)
                    - `roomId` : 채팅방 ID  
                    - `otherUserId` : 상대 유저의 User ID  
                    - `name` : 상대방 이름(모델) / 활동명(디자이너)  
                    - `profileImageUrl` : 상대방 프로필 이미지  
                    - `messageType` : 마지막 메시지 타입(TEXT / IMAGE)  
                    - `lastMessage` : 마지막 메시지 내용 (IMAGE인 경우 "이미지")  
                    - `lastMessageTime` : 마지막 메시지 생성 시간  
                    - `unreadMessages` : 해당 채팅방에서 **내가 아직 읽지 않은 메시지 수**  
                    - `role` : 상대방의 역할 (DESIGNER / MODEL)
                    """
    )
    ApiResponse<List<ChatRoomListResponse>> getMyChatRoomList(
            @AuthenticationPrincipal AuthDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "채팅 내역 조회 (cursor 기반 무한 스크롤)",
            description = """
                    특정 채팅방의 **상대 정보 + 메시지 히스토리**를 조회합니다.  
                    메시지는 항상 **시간 오름차순(과거 → 최신)**으로 반환되며,  
                    위로 스크롤하는 방식의 **cursor 기반 무한 스크롤**에 맞춰져 있습니다.
                    
                    ---
                    🔰 1) 최초 호출 (가장 최근 메시지 불러오기)
                    - `cursorMessageId` 없이 호출합니다. (`null`)  
                      - 예) `GET /chat/rooms/{roomId}/messages?size=20`
                    - 해당 채팅방의 **가장 최신 메시지들** 중 최대 `size`개를 내려줍니다.
                    - 응답의 `messages` 배열에서 **맨 아래에 있는 메시지**가 가장 최신 메시지입니다.
                    - 최초 진입 시, 서버에서 **상대가 보낸 안읽은 메시지를 모두 읽음 처리**합니다.
                    
                    ---
                    🔁 2) 과거 메시지 더 불러오기 (위로 스크롤)
                    - 프론트에서 현재 화면에 보여지고 있는 메시지들 중  
                      **가장 위에 있는(가장 오래된) 메시지의 `messageId`**를 `cursorMessageId`로 보냅니다.
                      
                      예)  
                      - 현재 화면에서 가장 위 메시지의 ID가 40이라면  
                        → `GET /chat/rooms/{roomId}/messages?cursorMessageId=40&size=20`
                      
                    - 서버는 `id < cursorMessageId` 인 메시지 중에서  
                      **더 과거의 메시지**를 최대 `size`개 반환합니다.
                    
                    ---
                    📤 Response (ChatRoomDetailResponse)
                    
                    - `roomId` : 채팅방 ID  
                    - `opponent` : 상대방 유저 정보 (userId, name, profileImageUrl, role)
                    
                    - `messages[]` : 조회된 메시지 리스트 (오래된 메시지 → 최신 메시지)
                      - `messageId` : 채팅 메시지 ID  
                      - `senderUserId` : 보낸 사람 User ID  
                      - `messageType` : TEXT / IMAGE  
                      - `message` : 텍스트 내용 / "이미지" (IMAGE 타입일 때)  
                      - `imageUrls` : IMAGE 타입일 때 S3 URL 리스트  
                      - `createdAt` : 메시지 생성 시간  
                      - `isRead` : **현재 유저 기준**으로 이 메시지가 읽음 처리되었는지 여부
                      
                    - `nextCursorMessageId` :
                      - 이번에 내려준 `messages` 중 **가장 오래된 메시지의 ID**
                      - 다음 요청 시 `cursorMessageId`로 그대로 넘겨주면 됩니다.
                      
                    - `hasNext` :
                      - `true`  : 아직 더 과거 메시지가 남아 있음 (스크롤 계속 가능)  
                      - `false` : 더 이상 불러올 메시지가 없음 (채팅의 시작 지점)
                      
                    - `lastReadMessageId` :
                      - **현재 유저가 읽은 상대 메시지들 중, 가장 마지막 메시지의 ID**
                      - 프론트에서 "내가 어디까지 읽었는지" 기준으로 사용하면 됩니다.
                    """
    )
    ApiResponse<ChatRoomDetailResponse> getChatRoomDetail(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursorMessageId,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "채팅 이미지 업로드 (S3) - WebSocket 이미지 메시지 전송 전에 호출",
            description = """
                    채팅방에서 이미지를 전송하기 위해 **이미지 파일을 먼저 S3에 업로드**하는 API입니다.  
                    이 API는 파일 업로드만 담당하며, 실제 채팅 메시지 전송은  
                    STOMP WebSocket으로 `imageUrls`를 포함한 메시지를 보내서 처리합니다.
                    
                    ---
                    📌 사용 흐름
                    
                    1️⃣ 프론트에서 이 API로 이미지 파일들을 업로드  
                    - `multipart/form-data` 형식으로 파일 전송  
                    - 서버는 S3에 업로드 후, 각 파일의 URL 리스트를 반환
                    
                    2️⃣ 프론트에서 STOMP 메시지 전송  
                    - `/pub/chat/rooms/{roomId}` 로 아래 형태의 메시지를 보냅니다:
                    
                    ```json
                    {
                      "messageType": "IMAGE",
                      "imageUrls": ["https://s3.../img1.png", "https://s3.../img2.png"]
                    }
                    ```
                    
                    ---
                    📥 Request
                    
                    - Path Variable
                      - `roomId` : 이미지를 업로드할 채팅방 ID  
                        → 서버에서 현재 유저가 이 방의 참여자인지 검증합니다.
                    
                    - Request Part
                      - `files` : 업로드할 이미지 파일 리스트 (`List<MultipartFile>`)
                    
                    ---
                    📤 Response
                    
                    - `List<String>` : 업로드된 이미지들의 S3 URL 리스트  
                      → 이 값을 WebSocket 메시지 전송 시 `imageUrls`에 그대로 넣어 사용하면 됩니다.
                    """
    )
    ApiResponse<List<String>> uploadChatImages(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long roomId,
            @org.springframework.web.bind.annotation.RequestPart("files") List<MultipartFile> files
    );
}

