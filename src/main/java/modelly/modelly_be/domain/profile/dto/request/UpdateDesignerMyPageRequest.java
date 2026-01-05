package modelly.modelly_be.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import modelly.modelly_be.domain.user.entity.enums.Gender;
import modelly.modelly_be.global.entity.Category;

import java.time.LocalDate;

@Schema(description = "디자이너 마이페이지 정보 수정 요청")
public record UpdateDesignerMyPageRequest(

        @NotNull
        @Size(max = 20)
        @Schema(description = "디자이너 활동명(닉네임)", example = "리아")
        String nickname,

        @NotNull
        @Schema(description = "성별", example = "FEMALE")
        Gender gender,

        @NotNull
        @Schema(description = "생년월일", example = "2001-01-01")
        LocalDate birth,

        @NotNull
        @Size(max = 100)
        @Schema(description = "한 줄 소개", example = "자연스러운 스타일을 추구하는 헤어 디자이너입니다.")
        String intro,

        @NotNull
        @Size(max = 50)
        @Schema(description = "매장 이름", example = "준오헤어")
        String shop,

        @NotNull
        @Size(max = 50)
        @Schema(description = "매장 주소(기본)", example = "서울특별시 서대문구 연세로 50")
        String addressLine1,

        @NotNull
        @Size(max = 50)
        @Schema(description = "매장 주소(상세)", example = "제4공학관 D504호")
        String addressLine2,

        @NotNull
        @Schema(description = "카테고리", example = "HAIR")
        Category category,

        @Size(max = 254)
        @Schema(description = "프로필 이미지 URL(선택)", nullable = true)
        String profileImageUrl
) {}
