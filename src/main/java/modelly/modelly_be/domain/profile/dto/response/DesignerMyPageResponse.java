package modelly.modelly_be.domain.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import modelly.modelly_be.domain.user.entity.enums.Gender;
import modelly.modelly_be.global.entity.Category;

import java.time.LocalDate;

@Schema(description = "디자이너 마이페이지 정보 조회 응답")
public record DesignerMyPageResponse(
        Long designerId,
        String nickname,
        String gender,
        LocalDate birth,
        String intro,
        String shop,
        Address address,
        String category,
        String profileImageUrl
) {
    public record Address(
            String line1, // 주소(기본)
            String line2 // 주소(상세)
    ) {}
}
