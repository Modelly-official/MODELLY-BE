package modelly.modelly_be.domain.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;

import java.time.LocalDate;

@Schema(description = "모델 마이페이지 정보 조회 응답")
public record ModelMyPageResponse(
        Long modelId,
        String nickname,
        String gender,
        LocalDate birth,
        String email,
        String profileImageUrl
) {

    public static ModelMyPageResponse from(Model model) {
        User user = model.getUser();
        return new ModelMyPageResponse(
                model.getId(),
                model.getNickname(),
                user.getGender().getDescription(),
                user.getBirth(),
                user.getEmail(),
                user.getImageUrl()
        );
    }
}
