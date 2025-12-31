package modelly.modelly_be.domain.recruitment.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.service.RecruitmentLikeService;
import modelly.modelly_be.domain.recruitment.dto.internal.CursorInformation;
import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentBasic;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.GuestRecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentDate;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import modelly.modelly_be.domain.recruitment.repository.RecruitmentTimeRepository;
import modelly.modelly_be.domain.reservation.dto.response.AvailableReservationScheduleResponse;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.domain.recruitment.repository.recruitmentRepository.RecruitmentRepository;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.review.dto.internal.AverageReview;
import modelly.modelly_be.domain.review.service.ReviewService;
import modelly.modelly_be.domain.user.dto.response.DesignerResponseDto;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.SortOption;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.Coordinate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentTimeRepository recruitmentTimeRepository;
    private final RecruitmentLikeService recruitmentLikeService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;

    public void save(Recruitment recruitment) {
        recruitmentRepository.save(recruitment);
    }

    public Recruitment getById(Long recruitmentId) {
        return recruitmentRepository.findById(recruitmentId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_RECRUITMENT));
    }

    @Transactional
    public void deleteRecruitment(Recruitment recruitment) {
        //관련된 찜 삭제
        recruitmentLikeService.deleteRecruitmentLike(recruitment);

        //관련된 예약 연관관계 삭제
        reservationService.deleteRelationshipWithRecruitment(recruitment);

        recruitmentRepository.delete(recruitment);
    }


    @Transactional(readOnly = true)
    public GuestRecruitmentResponseDto getByIdWithDesigner(Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findByIdWithAllDetails(recruitmentId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_RECRUITMENT));
        AverageReview averageReview = reviewService.calculateRating(recruitment.getDesigner());

        return GuestRecruitmentResponseDto.of(
                DesignerResponseDto.from(recruitment.getDesigner()),
                recruitment,
                averageReview
        );
    }

    public List<RecruitmentListResponseDto> getRecruitmensList(Long userId, SearchCondition searchCondition, SortOption sortOption, CursorInformation cursorInformation, int size, Coordinate userCoordinate) {
        List<RecruitmentBasic> recruitmentBasics;
        Long cursorId = cursorInformation.cursorId() == null || cursorInformation.cursorId() == 0 ? null : cursorInformation.cursorId();

        switch (sortOption){
            case NEWEST:
                recruitmentBasics = recruitmentRepository.findRecruitmentsByCreatedAt(userId,searchCondition, cursorId, size, userCoordinate);
                break;
            case MOST_REVIEWS:
                Long cursorReviewCount = cursorInformation.cursorReviewCount() == null ? null : cursorInformation.cursorReviewCount();
                recruitmentBasics = recruitmentRepository.findRecruitmentsByReviews(userId,searchCondition, cursorId, cursorReviewCount, size, userCoordinate);
                break;
            case DISTANCE:
                Double cursorDistance = cursorInformation.cursorDistance() == null ? null : cursorInformation.cursorDistance();
                recruitmentBasics = recruitmentRepository.findRecruitmentsByDistance(userId,searchCondition, cursorId, cursorDistance,size, userCoordinate);
                break;
            default:
               recruitmentBasics = recruitmentRepository.findRecruitmentsByCreatedAt(userId,searchCondition, cursorId, size, userCoordinate);
               break;
        }

        List<Long> recruitmentIds = recruitmentBasics.stream()
                .map(RecruitmentBasic::recruitmentId)
                .toList();


        Map<Long, Set<SubCategory>> setMap = recruitmentRepository.findSubCategoriesByRecruitmentIds(recruitmentIds);

        return recruitmentBasics.stream()
                .map(basic -> {
                    List<String> subCategories = setMap.getOrDefault(basic.recruitmentId(), Set.of())
                            .stream().map(SubCategory::getDescription)
                            .collect(Collectors.toList());

                    return new RecruitmentListResponseDto(
                            basic.recruitmentId(),
                            basic.title(),
                            basic.designerImage(),
                            basic.designerName(),
                            basic.recruitmentThumbnail(),
                            basic.shop(),
                            basic.shopAddress(),
                            basic.category().getDescription(),
                            subCategories,
                            basic.reviewCount(),
                            basic.distance() == 0.0? null: basic.distance(),
                            basic.isLiked(),
                            basic.createdAt(),
                            basic.averageRating()
                    );
                }).toList();
    }

    @Transactional
    public int updateStatusToClosed(LocalDate today) {
        return recruitmentRepository.updateStatusToClosed(today);
    }

    public List<DesignerRecruitmentListResponseDto> getByDesignerAndRecruitmentDate(Designer designer, YearMonth yearMonth, int size, LocalDate cursorEarliestDate, Long cursorId) {
        return recruitmentRepository.findRecruitmentsByDesignerAndDate(designer,yearMonth,size,cursorEarliestDate,cursorId);
    }

    // 공고의 특정 시간대 Lock(디자이너 기준으로 동일 시간대 전부)
    @Transactional
    public List<RecruitmentTime> getAllRecruitmentTimesForUpdate(Long designerId, LocalDate date, LocalTime startTime) {
        List<RecruitmentTime> slots = recruitmentTimeRepository.findAllTimeForUpdateByDesigner(designerId, date, startTime);

        if (slots.isEmpty()) {
            throw new GeneralException(ErrorStatus.NOT_FOUND_RECRUITMENT_TIME);
        }
        return slots;
    }

    // 특정 달의 예약 가능한 시간대 조회
    @Transactional(readOnly = true)
    public AvailableReservationScheduleResponse getAvailableSchedules(Long recruitmentId, String month) {
        YearMonth ym;
        if (month == null || month.isBlank()) {
            ym = YearMonth.now(ZoneId.of("Asia/Seoul"));
        } else {
            try {
                ym = YearMonth.parse(month); // yyyy-MM
            } catch (Exception e) {
                throw new GeneralException(ErrorStatus.MONTH_BAD_REQUEST);
            }
        }

        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.plusMonths(1).atDay(1);

        Recruitment recruitment = recruitmentRepository
                .findByIdWithScheduleInRange(recruitmentId, startDate, endDate)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_RECRUITMENT));

        DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

        var schedules = recruitment.getRecruitmentDates().stream()
                .filter(rd -> !rd.getDate().isBefore(startDate) && rd.getDate().isBefore(endDate))
                .sorted(Comparator.comparing(RecruitmentDate::getDate)) // 날짜 오름차순
                .map(rd -> new AvailableReservationScheduleResponse.DateSchedule(
                        rd.getDate(),
                        rd.getRecruitmentTimes().stream()
                                .sorted(Comparator.comparing(RecruitmentTime::getStartTime)) // 시간 오름차순
                                .map(rt -> new AvailableReservationScheduleResponse.TimeSlot(
                                        rt.getStartTime().format(HM),
                                        rt.isReserved()
                                ))
                                .toList()
                ))
                .toList();

        return new AvailableReservationScheduleResponse(recruitmentId, ym.toString(), schedules);
    }
}
