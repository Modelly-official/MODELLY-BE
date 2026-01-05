package modelly.modelly_be.domain.profile.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.profile.dto.request.UpdateDesignerProfileRequest;
import modelly.modelly_be.domain.profile.dto.response.DesignerProfileResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "디자이너 프로필 API",
        description = "디자이너 프로필 조회(게스트/모델), 디자이너 마이프로필 조회/수정"
)
public interface DesignerProfileSwagger {

    @Operation(
            summary = "디자이너 프로필 조회(게스트/모델) - 인증X",
            description = """
                    ### 특정 디자이너의 프로필 정보를 조회하는 API입니다. (인증X)\n
                    - 모델/게스트 누구나 조회 가능합니다.\n
                    - 디자이너 기본 정보 + 모집중 공고 목록을 함께 반환합니다.\n
                    - 모집중 공고 정렬 기준은 **deadline 오름차순 → recruitmentId 오름차순** 입니다.\n
                    \n
                    ---\n
                    ### Path Variable\n
                    - `designerId` : 조회할 디자이너 ID\n
                    \n
                    ---\n
                    ### Response\n
                    - `profile`\n
                      - `designerUserId` : 디자이너 유저 ID\n
                      - `designerId` : 디자이너 ID\n
                      - `nickname` : 디자이너 닉네임\n
                      - `profileImageUrl` : 프로필 이미지 URL\n
                      - `shop` : 매장 이름\n
                      - `address(line1, line2)` : 주소(line1: 도로명주소, line2: 상세주소) \n 
                      - `intro` : 한 줄 소개\n
                      - `isLiked` : 찜 여부(true: 찜 O, false: 찜 X)\n
                    - `openRecruitments[]` : 모집중 공고 카드 목록\n
                      - `recruitmentId` : 공고 ID\n
                      - `title` : 공고 제목\n
                      - `thumbnailUrl` : 공고 썸네일\n
                      - `startDate` : 시작일\n
                      - `deadline` : 마감일\n
                      - `subCategories` : 세부 카테고리 표시값 목록 (ex. ["펌","커트"])\n
                    """
    )
    ApiResponse<DesignerProfileResponse> getPublicProfile(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long designerId
    );

    @Operation(
            summary = "디자이너 프로필 조회(공개용 프로필)",
            description = """
                    ### 디자이너 본인의 프로필 정보를 조회하는 API입니다.\n
                    - **디자이너 권한만 호출 가능**합니다.\n
                    - 본인 프로필 + 모집중 공고 목록을 함께 반환합니다.\n
                    - 정렬/필드 규칙은 공개 프로필 조회와 동일합니다.\n
                    \n
                    ---\n
                    ### Response\n
                    - `profile`\n
                      - `designerUserId` : 디자이너 유저 ID\n
                      - `designerId` : 디자이너 ID\n
                      - `nickname` : 디자이너 닉네임\n
                      - `profileImageUrl` : 프로필 이미지 URL\n
                      - `shop` : 매장 이름\n
                      - `address(line1, line2)` : 주소(line1: 도로명주소, line2: 상세주소) \n 
                      - `intro` : 한 줄 소개\n
                    - `openRecruitments[]` : 모집중 공고 카드 목록\n
                      - `recruitmentId` : 공고 ID\n
                      - `title` : 공고 제목\n
                      - `thumbnailUrl` : 공고 썸네일\n
                      - `startDate` : 시작일\n
                      - `deadline` : 마감일\n
                      - `subCategories` : 세부 카테고리 표시값 목록 (ex. ["펌","커트"])\n
                    ---\n
                    ✅ 권한\n
                    - 디자이너 권한만 호출 가능합니다.\n
                    """
    )
    ApiResponse<DesignerProfileResponse> getMyProfile(
            @AuthenticationPrincipal AuthDetails auth
    );

    @Operation(
            summary = "디자이너 프로필 수정(공개용 프로필)",
            description = """
                    ### 디자이너가 본인 프로필 정보를 부분 수정(PATCH)하는 API입니다.\n
                    - **디자이너 권한만 호출 가능**합니다.\n
                    - 수정 가능한 항목: `nickname`, `intro`, `shop`, `addressLine1`, `addressLine2`, `profileImageUrl`\n
                    - Request Body에서 **null인 필드는 수정하지 않습니다.**\n
                    - 프로필 이미지 업로드는 Presigned URL 방식이며, 업로드 완료 후 `profileImageUrl`만 전달합니다.\n
                    \n
                    ---\n
                    ### Request Body\n
                    - `nickname`(optional) : 디자이너 닉네임 (max=20)\n
                    - `intro`(optional) : 한 줄 소개 (max=100)\n
                    - `shop`(optional) : 매장 이름 (max=50)\n
                    - `addressLine1`(optional) : 주소 (max=50)\n
                    - `addressLine2`(optional) : 상세 주소 (max=50)\n
                    - `profileImageUrl`(optional) : 프로필 이미지 URL (max=254)\n
                    \n
                    ---\n
                    ### Response\n
                    - 수정된 최신 프로필 + 모집중 공고 목록을 반환합니다.\n
                    \n
                    ---\n
                    ✅ 권한\n
                    - 디자이너 권한만 호출 가능합니다.\n
                    """
    )
    ApiResponse<DesignerProfileResponse> updateMyProfile(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestBody @Valid UpdateDesignerProfileRequest request
    );
}

