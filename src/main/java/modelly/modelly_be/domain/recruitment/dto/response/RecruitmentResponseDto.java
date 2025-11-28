package modelly.modelly_be.domain.recruitment.dto.response;

import modelly.modelly_be.domain.recruitment.dto.common.RecruitmentSchedule;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentImage;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

public record RecruitmentResponseDto(
        Long recruitmentId,
        String title,
        List<RecruitmentSchedule> recruitmentSchedule,
        Category category,
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

    public static RecruitmentResponseDto from(Recruitment recruitment) {
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

        return new RecruitmentResponseDto(
                recruitment.getId(),
                recruitment.getTitle(),
                schedules,
                recruitment.getCategory(),
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
