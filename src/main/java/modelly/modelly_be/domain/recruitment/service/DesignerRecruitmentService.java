package modelly.modelly_be.domain.recruitment.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentSchedule;
import modelly.modelly_be.domain.recruitment.dto.request.RecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.request.UpdateRecruitmentRequestDto;
import modelly.modelly_be.domain.recruitment.dto.internal.DesignerRecruitmentList;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponse;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentDate;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentImage;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.listener.dto.S3FolderDeleteEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignerRecruitmentService {

    private final RecruitmentService recruitmentService;
    private final DesignerService designerService;
    private final ReservationService reservationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Recruitment createRecruitment(User user, RecruitmentRequestDto recruitmentRequestDto) {
        checkDesigner(user);

        Designer designer = designerService.getByUser(user);

        Recruitment recruitment = Recruitment.builder()
                .designer(designer)
                .title(recruitmentRequestDto.title())
                .category(designer.getCategory())
                .notice(recruitmentRequestDto.notice())
                .content(recruitmentRequestDto.content())
                .restriction(recruitmentRequestDto.restriction())
                .goal1(recruitmentRequestDto.goal1())
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

        if (recruitmentRequestDto.subCategoryList() != null
                && !recruitmentRequestDto.subCategoryList().isEmpty()) {
            updateSubCategory(recruitment, designer.getCategory(), recruitmentRequestDto.subCategoryList());
        }

        recruitmentService.save(recruitment);

        //스케줄 설정
        updateSchedule(recruitment, recruitmentRequestDto.recruitmentSchedule());

        //공고 이미지 엔티티
        if (recruitmentRequestDto.imageUrls() != null) {
            recruitment.updateImageInf(recruitmentRequestDto.imageFolderId(), recruitmentRequestDto.thumbnail());
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

        //스케줄 수정
        if (requestDto.recruitmentSchedule()!= null) {
            recruitment.getRecruitmentDates().clear();
            updateSchedule(recruitment, requestDto.recruitmentSchedule());
        }

        //카테고리 수정
        if (requestDto.subCategoryList() != null && !requestDto.subCategoryList().isEmpty()) {
            recruitment.getSubCategoryList().clear();
            updateSubCategory(recruitment, designer.getCategory(), requestDto.subCategoryList());
        }

        if (requestDto.imageFolderId() != null && !requestDto.imageFolderId().equals(recruitment.getImageFolderId())) {

            //기존 S3 폴더 삭제
            if (recruitment.getImageFolderId() != null) {
                String oldFolderPath = "recruitments/" + recruitment.getImageFolderId() + "/";
                eventPublisher.publishEvent(new S3FolderDeleteEvent(oldFolderPath));
            }

            //DB에서 공고 이미지 리스트 삭제
            recruitment.getRecruitmentImages().clear();

            // 새로운 이미지 리스트 추가
            if (requestDto.imageUrls() != null) {
                for (String imageUrl : requestDto.imageUrls()) {
                    RecruitmentImage recruitmentImage = RecruitmentImage.builder()
                            .recruitment(recruitment)
                            .imageUrl(imageUrl)
                            .build();
                    recruitment.addImage(recruitmentImage);
                }
            }
            recruitment.updateImageInf(requestDto.imageFolderId(), requestDto.thumbnail());
        }

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

        //기존 이미지 삭제
        if (recruitment.getImageFolderId() != null) {
            String oldFolderPath = "recruitments/" + recruitment.getImageFolderId() + "/";
            eventPublisher.publishEvent(new S3FolderDeleteEvent(oldFolderPath));
        }

        recruitmentService.deleteRecruitment(recruitment);
    }

    private void isRecruitmentAuthor(Designer designer, Recruitment recruitment) {
        if (!recruitment.getDesigner().equals(designer)){
            throw new GeneralException(ErrorStatus.FORBIDDEN_DELETE_OR_MODIFY_RECRUITMENT);
        }
    }

    public List<DesignerRecruitmentListResponse> getDesginerRecruitments(User user, String month, int size, LocalDate cursorEarliestDate, Long cursorId) {
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new GeneralException(ErrorStatus.MONTH_BAD_REQUEST);
        }

        Designer designer = designerService.getByUser(user);

        List<DesignerRecruitmentListResponse> responseDtos = recruitmentService.getByDesignerAndRecruitmentDate(designer, yearMonth, size,cursorEarliestDate,cursorId);

        return responseDtos;
    }

    @Transactional
    public void updateSchedule(Recruitment recruitment, List<RecruitmentSchedule> recruitmentScheduleList){
        for (var scheduleDto : recruitmentScheduleList) {
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
    }

    @Transactional
    public void updateSubCategory(Recruitment recruitment, Category parentCategory,List<SubCategory> subCategoryList) {

            // 모든 서브 카테고리가 상위 카테고리에 속하는지 검증
            boolean isAllMatch = subCategoryList.stream()
                    .allMatch(sub -> sub != SubCategory.ETC? sub.getParentCategory() == parentCategory : true);

            if (!isAllMatch) {
                throw new GeneralException(ErrorStatus.SUBCATEGORY_MISMATCH);
            }

            recruitment.getSubCategoryList()
                    .addAll(subCategoryList);
    }
}
