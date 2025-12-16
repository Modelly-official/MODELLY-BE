package modelly.modelly_be.domain.recruitment.dto.response;

import modelly.modelly_be.domain.recruitment.dto.common.RecruitmentSchedule;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentImage;
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
    String goal2,
    String goal3,
    List<String> imageUrls,
    boolean agreeVideo,
    boolean agreeInsta,
    boolean agreeMosaic,
    String etc
) {

    public static GuestRecruitmentResponseDto of(DesignerResponseDto designerProfile, Recruitment recruitment){
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
                recruitment.getGoal2(),
                recruitment.getGoal3(),
                imageUrls,
                recruitment.isAgreeVideo(),
                recruitment.isAgreeInsta(),
                recruitment.isAgreeMosaic(),
                recruitment.getEtc()
        );
    }
}
