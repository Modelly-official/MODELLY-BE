package modelly.modelly_be.domain.recruitment.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.dto.RecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentDate;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentImage;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.formatter.TimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class DesignerRecruitmentService {

    private final RecruitmentService recruitmentService;
    private final DesignerService designerService;
    private final TimeFormatter timeFormatter;

    @Transactional
    public Recruitment createRecruitment(User user, RecruitmentRequestDto recruitmentRequestDto) {
        checkDesigner(user);

        Designer designer = designerService.getByUser(user);

        Recruitment recruitment = Recruitment.builder()
                .designer(designer)
                .title(recruitmentRequestDto.title())
                .category(recruitmentRequestDto.category())
                .notice(recruitmentRequestDto.notice())
                .content(recruitmentRequestDto.content())
                .goal1(recruitmentRequestDto.goal1())
                .goal2(recruitmentRequestDto.goal2())
                .goal3(recruitmentRequestDto.goal3())
                .agreeInsta(recruitmentRequestDto.agreeInsta())
                .agreeMosaic(recruitmentRequestDto.agreeMosaic())
                .agreeVideo(recruitmentRequestDto.agreeVideo())
                .etc(recruitmentRequestDto.etc())
                .build();

        recruitmentService.save(recruitment);

        //스케줄 설정
        for (var scheduleDto : recruitmentRequestDto.recruitmentSchedule()) {
            RecruitmentDate date = RecruitmentDate.builder()
                    .recruitment(recruitment)
                    .date(scheduleDto.recruitmentDate())
                    .build();
            recruitment.addDate(date);

            for (String time : scheduleDto.recruitmentTimes()) {
                LocalTime localTime = LocalTime.parse(time);
                RecruitmentTime recruitmentTime = RecruitmentTime.builder()
                        .recruitmentDate(date)
                        .startTime(localTime)
                        .build();

                date.addTime(recruitmentTime);
            }
        }

        //공고 이미지 엔티티
        for (String imageUrl : recruitmentRequestDto.imageUrls()) {
            RecruitmentImage recruitmentImage = RecruitmentImage.builder()
                    .recruitment(recruitment)
                    .imageUrl(imageUrl)
                    .build();

            recruitment.addImage(recruitmentImage);
        }

        return recruitment;
    }

    public void checkDesigner(User user){
        if (user.getUserRole() != UserRole.DESIGNER){
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
    }
}
