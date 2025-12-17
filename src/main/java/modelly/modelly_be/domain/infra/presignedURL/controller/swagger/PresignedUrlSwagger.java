package modelly.modelly_be.domain.infra.presignedURL.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import modelly.modelly_be.domain.infra.presignedURL.dto.PresignedUrlListResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "이미지 업로드 (Presigned URL) 관련 API")
public interface PresignedUrlSwagger {

    @Operation(summary = "공고 이미지용 Presigned URL 발급 API", description = """
            디자이너가 공고 생성 시 이미지 업로드할 때 필요한 Presigned URL을 발급받는 API입니다. \n
            ---
            ✅ 접근 제어
            - 요청한 사용자가 디자이너인지 확인합니다.
            
            ---
            Request Parameter
            - `imageCount` : 업로드할 이미지 개수 (최대 3장까지만 가능합니다.)
            
            ---
            🧩 프론트엔드 처리 흐름 \n
            1️⃣ Presigned URL 발급 요청 \n
            `POST /presigned-url/recruitments`
            
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
            @RequestParam @Max(3) int imageCount
    );

    @Operation(summary = "리뷰 이미지용 Presigned URL 발급 API", description = """
            디자이너가 공고 생성 시 이미지 업로드할 때 필요한 Presigned URL을 발급받는 API입니다. \n
            ---
            ✅ 접근 제어
            - 요청한 사용자가 모델인지 확인하고, 해당 예약의 주인이 맞는지 확인합니다.
            
            ---
            Request Parameter
            - `imageCount` : 업로드할 이미지 개수 (최대 3장까지만 가능합니다.)
            
            ---
            🧩 프론트엔드 처리 흐름 \n
            1️⃣ Presigned URL 발급 요청 \n
            `POST /presigned-url/reviews`
            
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
            @RequestParam @Max(3) int imageCount
    );
}
