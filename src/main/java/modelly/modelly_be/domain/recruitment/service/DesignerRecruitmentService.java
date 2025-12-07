package modelly.modelly_be.domain.recruitment.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.dto.request.RecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.request.UpdateRecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentDate;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentImage;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignerRecruitmentService {

    private final RecruitmentService recruitmentService;
    private final DesignerService designerService;
    private final ReservationService reservationService;

    @Transactional
    public Recruitment createRecruitment(User user, RecruitmentRequestDto recruitmentRequestDto) {
        checkDesigner(user);

        Designer designer = designerService.getByUser(user);

        Recruitment recruitment = Recruitment.builder()
                .designer(designer)
                .title(recruitmentRequestDto.title())
                .category(recruitmentRequestDto.category())
                .subCategory(recruitmentRequestDto.subCategory())
                .notice(recruitmentRequestDto.notice())
                .content(recruitmentRequestDto.content())
                .goal1(recruitmentRequestDto.goal1())
                .goal2(recruitmentRequestDto.goal2())
                .goal3(recruitmentRequestDto.goal3())
                .agreeInsta(recruitmentRequestDto.agreeInsta())
                .agreeMosaic(recruitmentRequestDto.agreeMosaic())
                .agreeVideo(recruitmentRequestDto.agreeVideo())
                .etc(recruitmentRequestDto.etc())
                .deadline(
                        recruitmentRequestDto.recruitmentSchedule().stream()
                                .map(rs -> rs.recruitmentDate())
                                .max(Comparator.naturalOrder())
                                .orElseThrow(() -> new IllegalArgumentException("schedule is required"))
                )
                .recruitmentStatus(RecruitmentStatus.OPEN)
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
        if (recruitmentRequestDto.imageUrls() != null) {
            for (String imageUrl : recruitmentRequestDto.imageUrls()) {
                if (imageUrl != null) {
                    RecruitmentImage recruitmentImage = RecruitmentImage.builder()
                            .recruitment(recruitment)
                            .imageUrl(imageUrl)
                            .build();

                    recruitment.addImage(recruitmentImage);
                }
            }
        }

        return recruitment;
    }

    public void checkDesigner(User user){
        if (user.getUserRole() != UserRole.DESIGNER){
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
    }

    @Transactional
    public RecruitmentResponseDto updateRecruitment(User user, Long recruitmentId, UpdateRecruitmentRequestDto requestDto) {
        checkDesigner(user);
        Designer designer = designerService.getByUser(user);

        Recruitment recruitment = recruitmentService.getById(recruitmentId);

        isRecruitmentAuthor(designer,recruitment);

        //이미 확정된 예약이 있는 경우 예외처리
        reservationService.hasPendingOrConfirmedReservation(recruitment);

        //수정 로직
        recruitment.updateRecruitment(requestDto);
        recruitmentService.save(recruitment);

        return RecruitmentResponseDto.from(recruitment);
    }

    @Transactional
    public void deleteRecruitment(User user, Long recruitmentId) {
        checkDesigner(user);
        Designer designer = designerService.getByUser(user);

        Recruitment recruitment = recruitmentService.getById(recruitmentId);

        isRecruitmentAuthor(designer,recruitment);

        //제약조건 체크
        reservationService.hasPendingOrConfirmedReservation(recruitment);

        recruitmentService.deleteRecruitment(recruitment);
    }

    private void isRecruitmentAuthor(Designer designer, Recruitment recruitment) {
        if (!recruitment.getDesigner().equals(designer)){
            throw new GeneralException(ErrorStatus.FORBIDDEN_DELETE_OR_MODIFY_RECRUITMENT);
        }
    }

    public List<DesignerRecruitmentListResponseDto> getDesginerRecruitments(User user, String month, int size) {
        checkDesigner(user);
        Designer designer = designerService.getByUser(user);

        List<DesignerRecruitmentListResponseDto> responseDtos = recruitmentService.getByDesignerAndRecruitmentDate(designer, month, size);

        return responseDtos;
    }
}
