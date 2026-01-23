package modelly.modelly_be.domain.profile.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DesignerProfileResponse(
        DesignerProfileInfo profile,
        List<RecruitmentCard> openRecruitments
) {
    public static DesignerProfileResponse of(
            DesignerProfileInfo profile,
            List<RecruitmentCard> openRecruitments
    ) {
        return new DesignerProfileResponse(profile, openRecruitments);
    }

    public record DesignerProfileInfo(
            Long designerUserId,
            Long designerId,
            String nickname,
            String profileImageUrl,
            String shop,
            Address address,
            String intro,
            boolean isLiked,
            Long reviewCount,
            double averageRating
    ) {}

    public record Address(String line1, String line2) {}

    public record RecruitmentCard(
            Long recruitmentId,
            String title,
            String thumbnailUrl,
            LocalDate startDate,
            LocalDate deadline,
            List<String> subCategories
    ) {}
}
