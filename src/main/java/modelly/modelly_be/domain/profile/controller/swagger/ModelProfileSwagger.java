package modelly.modelly_be.domain.profile.controller.swagger;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerMyPageRequest;
import modelly.modelly_be.domain.profile.dto.request.UpdateModelMyPageRequest;
import modelly.modelly_be.domain.profile.dto.response.ModelMyPageResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "모델 프로필 API",
        description = "모델 마이페이지 조회/수정"
)
public interface ModelProfileSwagger {
    @Operation(
            summary = "모델 마이페이지 프로필 조회(마이페이지)",
            description = """
                ### 모델 마이페이지(내 정보 수정 화면)에서 사용할 정보를 조회하는 API입니다.\n
                - **모델 권한만 호출 가능**합니다.\n
                - 모델 활동명/성별/생년월일/프로필 이미지 URL을 함께 반환합니다.\n
                \n
                ---\n
                ### Response\n
                - `modelId` : 모델 ID\n
                - `nickname` : 모델 활동명(닉네임)\n
                - `gender` : 성별 표시값 (ex. \"여자\")\n
                - `birth` : 생년월일 (YYYY-MM-DD)\n
                - `profileImageUrl` : 프로필 이미지 URL\n
                \n
                ---\n
                ✅ 권한\n
                - 모델 권한만 호출 가능합니다.\n
                """
    )
    ApiResponse<ModelMyPageResponse> getMyPage(
            @AuthenticationPrincipal AuthDetails auth
    );

    @Operation(
            summary = "모델 마이페이지 프로필 수정(마이페이지)",
            description = """
                ### 모델 마이페이지(내 정보 수정 화면) 정보를 수정하는 API입니다.\n
                - **모델 권한만 호출 가능**합니다.\n
                - PUT 방식이며, **요청에 포함된 값으로 전체 업데이트**합니다.\n
                - 단, `profileImageUrl`은 선택값이며 **null이면 이미지 URL은 변경하지 않습니다.**\n
                \n
                ---\n
                ### Request Body\n
                - `nickname`(required) : 모델 활동명(닉네임) (max=20)\n
                - `gender`(required) : 성별 (MALE/FEMALE)\n
                - `birth`(required) : 생년월일 (YYYY-MM-DD)\n
                - `profileImageUrl`(optional) : 프로필 이미지 URL (max=254)\n
                \n
                ---\n
                ### Response\n
                - 수정된 최신 마이페이지 정보를 반환합니다.\n
                \n
                ---\n
                ✅ 권한\n
                - 모델 권한만 호출 가능합니다.\n
                """
    )
    ApiResponse<ModelMyPageResponse> updateMyPage(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestBody @Valid UpdateModelMyPageRequest req
    );
}
