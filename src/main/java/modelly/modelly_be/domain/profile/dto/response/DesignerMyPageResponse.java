package modelly.modelly_be.domain.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;

import java.time.LocalDate;

@Schema(description = "디자이너 마이페이지 정보 조회 응답")
public record DesignerMyPageResponse(
        Long designerId,
        String nickname,
        String gender,
        LocalDate birth,
        String email,
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

    public static DesignerMyPageResponse from(Designer designer) {
        User user = designer.getUser();
        return new DesignerMyPageResponse(
                designer.getId(),
                designer.getNickname(),
                user.getGender().getDescription(),
                user.getBirth(),
                user.getEmail(),
                designer.getIntro(),
                designer.getShop(),
                new Address(designer.getAddressLine1(), designer.getAddressLine2()),
                designer.getCategory().getDescription(),
                user.getImageUrl()
        );
    }
}
