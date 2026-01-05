package modelly.modelly_be.domain.profile.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDesignerProfileRequest(

        @NotNull
        @Size(max = 20)
        @Schema(description = "닉네임", example = "리아")
        String nickname,

        @NotNull
        @Size(max = 100)
        @Schema(description = "한줄 소개", example = "레이어드/허쉬컷 전문 디자이너입니다.")
        String intro,

        @NotNull
        @Size(max = 50)
        @Schema(description = "매장 이름", example = "준오헤어")
        String shop,

        @NotNull
        @Size(max = 50)
        @Schema(description = "매장 주소(기본)", example = "서울시 강남구 테헤란로 1234")
        String addressLine1,

        @NotNull
        @Size(max = 50)
        @Schema(description = "매장 주소(상세)", example = "2층")
        String addressLine2,

        @Size(max = 254)
        String profileImageUrl
) {}
