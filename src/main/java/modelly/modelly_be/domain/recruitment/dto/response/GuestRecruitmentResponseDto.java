package modelly.modelly_be.domain.recruitment.dto.response;

import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentSchedule;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentImage;
import modelly.modelly_be.domain.review.dto.internal.AverageReview;
import modelly.modelly_be.domain.user.dto.response.DesignerResponseDto;

import java.util.List;

public record GuestRecruitmentResponseDto(
    DesignerResponseDto designerProfile,
    Long recruitmentId,
    String title,
    List<RecruitmentSchedule> recruitmentSchedule,
    String category,
    List<String> subCategories,
    String content,
    String notice,
    String goal1,
    List<String> imageUrls,
    boolean agreeVideo,
    boolean agreeInsta,
    boolean agreeMosaic,
    String etc,
    Long reviewCount,
    double averageRating
) {

    public static GuestRecruitmentResponseDto of(DesignerResponseDto designerProfile, Recruitment recruitment, AverageReview averageReview) {
        List<RecruitmentSchedule> schedules = recruitment.getRecruitmentDates().stream()
                .map(date -> RecruitmentSchedule.of(date.getDate(),
                        date.getRecruitmentTimes().stream()
                                .map(recruitmentTime -> recruitmentTime.getStartTime().toString())
                                .toList()
                ))
                .toList();

        List<String> imageUrls = recruitment.getRecruitmentImages().stream()
                .map(RecruitmentImage::getImageUrl)
                .toList();

        List<String> subCategories = recruitment.getSubCategoryList().stream()
                .map(subCategory -> subCategory.getDescription())
                .toList();

        return new GuestRecruitmentResponseDto(
                designerProfile,
                recruitment.getId(),
                recruitment.getTitle(),
                schedules,
                recruitment.getCategory().getDescription(),
                subCategories,
                recruitment.getContent(),
                recruitment.getNotice(),
                recruitment.getGoal1(),
                imageUrls,
                recruitment.isAgreeVideo(),
                recruitment.isAgreeInsta(),
                recruitment.isAgreeMosaic(),
                recruitment.getEtc(),
                averageReview.totalCount(),
                averageReview.averageRating()
        );
    }
}
