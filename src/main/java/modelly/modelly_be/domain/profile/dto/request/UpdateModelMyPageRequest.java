package modelly.modelly_be.domain.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import modelly.modelly_be.domain.user.entity.enums.Gender;
import modelly.modelly_be.global.entity.Category;

import java.time.LocalDate;

@Schema(description = "모델 마이페이지 정보 수정 요청")
public record UpdateModelMyPageRequest(

        @NotNull
        @Size(max = 20)
        @Schema(description = "모델(닉네임)", example = "리아")
        String nickname,

        @NotNull
        @Schema(description = "성별", example = "FEMALE")
        Gender gender,

        @NotNull
        @Schema(description = "생년월일", example = "2001-01-01")
        LocalDate birth,

        @Size(max = 254)
        @Schema(description = "프로필 이미지 URL(선택)", nullable = true)
        String profileImageUrl
) {}
