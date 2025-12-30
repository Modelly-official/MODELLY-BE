package modelly.modelly_be.domain.infra.presignedURL.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import modelly.modelly_be.domain.infra.presignedURL.dto.PresignedUrlListResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.s3.PresignedUploadResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "이미지 업로드 (Presigned URL) 관련 API")
public interface PresignedUrlSwagger {

    @Operation(summary = "공고 이미지용 Presigned URL 발급 API", description = """
            디자이너가 공고 생성 / 수정 시 이미지 업로드할 때 필요한 Presigned URL을 발급받는 API입니다. \n
            ---
            ✅ 접근 제어
            - 요청한 사용자가 디자이너인지 확인합니다.
            
            ---
            Request Parameter
            - `imageCount` : 업로드할 이미지 개수 (최대 3장까지만 가능합니다.)
            
            ---
            🧩 프론트엔드 처리 흐름 \n
            1️⃣ Presigned URL 발급 요청 \n
            `GET /presigned-url/recruitments`
            
            2️⃣ S3에 직접 파일 업로드 \n
            ```
            PUT {uploadUrl}
            Body: file(binary)
            ```
            
            ❗️배열 0번째 presignedUrl(쌍인 imageUrl의 끝이 ~~~_main인 Url)로 업로드한 이미지를 바탕으로 썸네일을 만드니 참고 부탁드립니다.
            
            3️⃣ 업로드가 완료되면 imageUrl 리스트(공고 이미지 리스트)와 thumbnailUrl(썸네일)을 공고 생성 시 이미지url 리스트에 담아서 전송
            """)
    @GetMapping("/presigned-url/recruitments")
    ApiResponse<PresignedUrlListResponse> createRecruitmentImage(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam @Validated @Max(3) int imageCount
    );

    @Operation(summary = "리뷰 이미지용 Presigned URL 발급 API", description = """
            모델이 리뷰 생성 / 수정 시 이미지 업로드할 때 필요한 Presigned URL을 발급받는 API입니다. \n
            ---
            ✅ 접근 제어
            - 요청한 사용자가 모델인지 확인하고, 해당 예약의 주인이 맞는지 확인합니다.
            
            ---
            Request Parameter
            - `imageCount` : 업로드할 이미지 개수 (최대 3장까지만 가능합니다.)
            
            ---
            🧩 프론트엔드 처리 흐름 \n
            1️⃣ Presigned URL 발급 요청 \n
            `GET /presigned-url/reviews`
            
            2️⃣ S3에 직접 파일 업로드 \n
            ```
            PUT {uploadUrl}
            Body: file(binary)
            ```
            
            ❗️배열 0번째 presignedUrl(쌍인 imageUrl의 끝이 ~~~_main인 Url)로 업로드한 이미지를 바탕으로 썸네일을 만드니 참고 부탁드립니다.
            
            3️⃣ 업로드가 완료되면 imageUrl 리스트(공고 이미지 리스트)와 thumbnailUrl(썸네일) 리뷰 생성 시 이미지url 리스트에 담아서 전송
            """)
    @GetMapping("/presigned-url/reviews")
    ApiResponse<PresignedUrlListResponse> createReviewsImage(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam Long reservationId,
            @RequestParam @Validated @Max(3) int imageCount
    );

    @Operation(summary = "포트폴리오 이미지용 Presigned URL 발급 API", description = """
            디자이너가 포트폴리오 이미지 업로드할 때 필요한 Presigned URL을 발급받는 API입니다. \n
            ---
            ✅ 접근 제어
            - 요청한 사용자가 디자이너인지 확인합니다.
            
            ---
            Request Parameter
            - `imageCount` : 업로드할 이미지 개수 (최대 3장까지만 가능합니다.)
            
            ---
            🧩 프론트엔드 처리 흐름 \n
            1️⃣ Presigned URL 발급 요청 \n
            `GET /presigned-url/portfolios`
            
            2️⃣ S3에 직접 파일 업로드 \n
            ```
            PUT {uploadUrl}
            Body: file(binary)
            ```
            
            ❗️배열 0번째 presignedUrl(쌍인 imageUrl의 끝이 ~~~_main인 Url)로 업로드한 이미지를 바탕으로 썸네일을 만드니 참고 부탁드립니다.
            
            3️⃣ 업로드가 완료되면 imageUrl 리스트(공고 이미지 리스트)와 thumbnailUrl(썸네일)을 공고 생성 시 이미지url 리스트에 담아서 전송
            """)
    @GetMapping("/presigned-url/portfolios")
    ApiResponse<PresignedUrlListResponse> createPortfolioImage(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam @Validated @Max(3) int imageCount
    );

    @Operation(
            summary = "채팅 이미지 업로드용 Presigned URL 발급",
            description = """
                채팅방에 업로드할 파일을 위해 **S3 Presigned PUT URL**을 발급합니다.  
                **프론트엔드가 S3로 직접 PUT 업로드**
                
                ---
                ✅ 접근 제어
                - 요청한 사용자가 해당 `roomId` 채팅방의 **참여자인지 검증**합니다.
                - 채팅방 참여자가 아닌 경우 **403 Forbidden** 에러가 발생합니다.
                
                ---
                📥 Request
                
                - Path Variable
                  - `roomId` : 채팅방 ID
                
                
                ---
                📤 Response (PresignedUploadResponse)
                
                - `uploadUrl`  
                  - S3에 **PUT 업로드**할 Presigned URL  
                  - 유효시간: **5분**
                
                - `imageUrl`  
                  - 업로드 완료 후 채팅 메시지로 사용할 **S3 객체 접근 URL**
                
                ---
                🧩 프론트엔드 처리 흐름
                
                1️⃣ Presigned URL 발급 요청  
                ```
                POST /chat/rooms/{roomId}/images/presigned
                ```
                
                2️⃣ S3에 직접 파일 업로드  
                ```
                PUT {uploadUrl}
                Body: file(binary)
                ```
                
                3️⃣ 업로드가 완료되면 `imageUrl`을 STOMP로 전송  
                - destination: `/pub/chat/rooms/{roomId}`
                - payload 예시:
                ```json
                {
                  "messageType": "IMAGE",
                  "imageUrls": ["{imageUrl}"]
                }
                ```
                
                4️⃣ 서버는 전달받은 URL들을 `chatting_image` 테이블에 저장하고,  
                같은 채팅방을 구독 중인 사용자들에게 IMAGE 메시지를 브로드캐스트합니다.
                
                """
    )
    @PostMapping("/chat/rooms/{roomId}/images/presigned")
    ApiResponse<PresignedUploadResponse> createPresignedUrl(
            @PathVariable Long roomId,
            @AuthenticationPrincipal AuthDetails auth
    );

    @Operation(
            summary = "프로필 이미지 업로드용 Presigned URL 발급 (인증 불필요)",
            description = """
            회원가입/프로필 설정 시 사용할 **프로필 이미지 업로드용 S3 Presigned PUT URL**을 발급합니다.  
            **프론트엔드가 S3로 직접 PUT 업로드**합니다.

            ---
            ✅ 접근 제어
            - **인증 없이 누구나 호출 가능**합니다. (permitAll)
            - 업로드 완료 후 반환된 `imageUrl`을 회원가입 또는 프로필 업데이트 요청에 담아 전송합니다.

            ---
            📤 Response (PresignedUploadResponse)

            - `uploadUrl`
              - S3에 **PUT 업로드**할 Presigned URL
              - 유효시간: **5분**

            - `imageUrl`
              - 업로드 완료 후 DB에 저장할 **S3 객체 접근 URL**

            ---
            🧩 프론트엔드 처리 흐름

            1️⃣ Presigned URL 발급 요청
            ```
            GET /presigned-url/profiles
            ```

            2️⃣ S3에 직접 파일 업로드
            ```
            PUT {uploadUrl}
            Headers:
              Content-Type: image/*   (프론트에서 파일 타입에 맞게 설정 권장)
            Body: file(binary)
            ```

            3️⃣ 업로드가 완료되면 `imageUrl`을 저장 API에 전달
            - 회원가입:
              - `POST /auth/signup` 의 `base.imageUrl`에 `{imageUrl}` 포함
            """
    )
    @GetMapping("/presigned-url/profiles")
    ApiResponse<PresignedUploadResponse> createProfilePresignedUrl();

    @Operation(
            summary = "예약 이미지 업로드용 Presigned URL 발급 (모델)",
            description = """
                예약 생성 시 사용할 **참고 이미지(1장)** 업로드를 위해 S3 Presigned PUT URL을 발급합니다.  
                **프론트엔드가 S3로 직접 PUT 업로드**한 뒤, 반환된 `imageUrl`을 예약 생성 요청에 포함합니다.

                ---
                ✅ 접근 제어
                - 요청한 사용자가 **모델인지 확인**합니다.
                - 모델이 아닌 경우 **403 Forbidden** 에러가 발생합니다.

                ---
                📥 Request
                - 별도의 Request Body 없음

                ---
                📤 Response (PresignedUploadResponse)

                - `uploadUrl`
                  - S3에 **PUT 업로드**할 Presigned URL
                  - 유효시간: **5분**

                - `imageUrl`
                  - 업로드 완료 후 예약 생성 시 `imageUrl` 필드에 넣을 **S3 객체 접근 URL**

                ---
                🧩 프론트엔드 처리 흐름

                1️⃣ Presigned URL 발급 요청  
                ```
                GET /presigned-url/reservations
                ```

                2️⃣ S3에 직접 파일 업로드  
                ```
                PUT {uploadUrl}
                Headers:
                  Content-Type: image/*   (파일 타입에 맞게 설정 권장)
                Body: file(binary)
                ```

                3️⃣ 업로드 완료 후 예약 생성 요청에 `imageUrl` 포함  
                - 예: `POST /models/reservations`
                ```json
                {
                  "recruitmentId": 1,
                  "date": "2025-12-30",
                  "startTime": "14:00",
                  "category": "HAIR",
                  "subCategories": ["CUT", "PERM"],
                  "comment": "앞머리만 가볍게",
                  "designerName": "준영쌤",
                  "shop": "어딘가헤어",
                  "imageUrl": "{imageUrl}"
                }
                ```
                """
    )
    @GetMapping("/presigned-url/reservations")
    ApiResponse<PresignedUploadResponse> createReservationImage(
            @AuthenticationPrincipal AuthDetails authDetails
    );
}
