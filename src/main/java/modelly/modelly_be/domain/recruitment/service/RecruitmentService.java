package modelly.modelly_be.domain.recruitment.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.home.dto.response.PopularRecruitmentListResponse;
import modelly.modelly_be.domain.like.service.RecruitmentLikeService;
import modelly.modelly_be.domain.recruitment.dto.internal.CursorInformation;
import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentBasic;
import modelly.modelly_be.domain.recruitment.dto.internal.DesignerRecruitmentList;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponse;
import modelly.modelly_be.domain.recruitment.dto.response.GuestRecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentDate;
import modelly.modelly_be.domain.recruitment.entity.RecruitmentTime;
import modelly.modelly_be.domain.recruitment.entity.enums.RecruitmentStatus;
import modelly.modelly_be.domain.recruitment.repository.RecruitmentTimeRepository;
import modelly.modelly_be.domain.reservation.dto.internal.ReservationEditInfoMap;
import modelly.modelly_be.domain.reservation.dto.response.AvailableReservationScheduleResponse;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationStatus;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.domain.user.service.UserService;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.domain.recruitment.repository.recruitmentRepository.RecruitmentRepository;
import modelly.modelly_be.domain.review.dto.internal.AverageReview;
import modelly.modelly_be.domain.review.service.ReviewService;
import modelly.modelly_be.domain.user.dto.response.DesignerResponseDto;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.SortOption;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
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
    private final ReviewService reviewService;
    private final ModelService modelService;
    private final UserService userService;
    private final ReservationService reservationService;

    public void save(Recruitment recruitment) {
        recruitmentRepository.save(recruitment);
    }

    public Recruitment getById(Long recruitmentId) {
        return recruitmentRepository.findById(recruitmentId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_RECRUITMENT));
    }

    @Transactional
    public void deleteRecruitment(Recruitment recruitment) {

        recruitmentRepository.delete(recruitment);
    }


    @Transactional(readOnly = true)
    public GuestRecruitmentResponseDto getByIdWithDesigner(Long userId, Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findByIdWithAllDetails(recruitmentId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_RECRUITMENT));

        AverageReview averageReview = reviewService.calculateRating(recruitment.getDesigner());

        boolean isLiked = false;
        boolean modelHasPendingReservation = false;
        boolean designerHasPendingReservation = false;
        boolean designerHasConfirmedReservation = false;

        if (userId != null){

            User user = userService.getById(userId);

            if (user.getUserRole() == UserRole.MODEL){

                Model model = modelService.getModelByUserId(userId);
                isLiked = recruitmentLikeService.existsByModelAndRecruitment(model, recruitment);

                // 이미 예약 신청(PENDING)을 했는지 확인
                modelHasPendingReservation =
                        reservationService.hasReservationByModelAndRecruitment(
                                model.getId(),
                                recruitment.getId(),
                                List.of(ReservationStatus.RESERVATION_PENDING)
                        );

            }
            // DESIGNER 기준 (공고 전체)
            designerHasPendingReservation =
                    reservationService.hasPendingReservationByRecruitment(
                            recruitment.getId()
                    );

            designerHasConfirmedReservation =
                    reservationService.hasOngoingConfirmedReservation(
                            recruitment.getId()
                    );

        }
        boolean canModify = !designerHasConfirmedReservation && !designerHasPendingReservation;

        return GuestRecruitmentResponseDto.of(
                DesignerResponseDto.from(recruitment.getDesigner()),
                recruitment,
                averageReview,
                isLiked,
                modelHasPendingReservation,
                designerHasPendingReservation,
                designerHasConfirmedReservation,
                canModify
        );
    }

    public ScrollResponse<RecruitmentListResponseDto> getRecruitmensList(Long userId, SearchCondition searchCondition, SortOption sortOption, CursorInformation cursorInformation, int size, Coordinate userCoordinate) {
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

        List<RecruitmentListResponseDto> recruitments = recruitmentBasics.stream()
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

        Long totalCount = countByCondition(searchCondition);

        ScrollResponse<RecruitmentListResponseDto> responseDtos = ScrollUtil.paginate(recruitments,size, totalCount);

        return responseDtos;
    }

    @Transactional
    public int updateStatusToClosed(LocalDate today) {
        return recruitmentRepository.updateStatusToClosed(today);
    }

    public List<DesignerRecruitmentListResponse> getByDesignerAndRecruitmentDate(Designer designer, YearMonth yearMonth, int size, LocalDate cursorEarliestDate, Long cursorId, RecruitmentStatus status) {
        List<DesignerRecruitmentList> recruitmentLists = recruitmentRepository.findRecruitmentsByDesignerAndDate(designer,yearMonth,size,cursorEarliestDate,cursorId, status);

        List<Long> recruitmentIds = recruitmentLists.stream()
                .map(DesignerRecruitmentList::recruitmentId)
                .toList();

        Map<Long, Set<SubCategory>> setMap = recruitmentRepository.findSubCategoriesByRecruitmentIds(recruitmentIds);

        ReservationEditInfoMap editInfoMap = reservationService.getEditInfoMap(recruitmentIds);

        return recruitmentLists.stream()
                .map(recruitment -> {
                    Long id = recruitment.recruitmentId();

                    List<String> subCategories = setMap.getOrDefault(id, Set.of())
                            .stream().map(SubCategory::getDescription)
                            .collect(Collectors.toList());

                    // 수정/삭제 가능 여부 파악
                    boolean hasPending = editInfoMap.pendingRecruitmentIds().contains(id);

                    boolean hasConfirmed = editInfoMap.confirmedRecruitmentIds().contains(id);

                    boolean canModify = !hasPending && !hasConfirmed;

                    return new DesignerRecruitmentListResponse(
                            id,
                            recruitment.title(),
                            recruitment.period(),
                            recruitment.thumbnail(),
                            recruitment.reviewCount(),
                            recruitment.averageRating(),
                            subCategories,
                            hasPending,
                            hasConfirmed,
                            canModify
                    );
                })
                .toList();

    }

    // 공고의 특정 시간대 Lock(디자이너 기준으로 동일 시간대 전부)
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

    public boolean isSlotAvailable(Designer designer, LocalDate date, LocalTime start) {

        return recruitmentTimeRepository.existsAvailableSlotForDesigner(
                designer.getId(), date, start
        );
    }

    @Transactional(readOnly = true)
    public Long countDesignerRecruitmentsByCondition(Designer designer, YearMonth yearMonth, RecruitmentStatus status) {
        return recruitmentRepository.countByDesignerAndDateAndStatus(
                designer,
                yearMonth.getYear(),
                yearMonth.getMonthValue(),
                status
        );
    }

    @Transactional(readOnly = true)
    public List<RecruitmentListResponseDto> getNearByRecruitments(Long userId, Coordinate userCoordinate, Category category){

        List<RecruitmentBasic> recruitmentBasics = recruitmentRepository.findNearbyRecruitments(userId, userCoordinate, category);

        List<Long> recruitmentIds = recruitmentBasics.stream()
                .map(RecruitmentBasic::recruitmentId)
                .toList();

        Map<Long, Set<SubCategory>> setMap = recruitmentRepository.findSubCategoriesByRecruitmentIds(recruitmentIds);

        List<RecruitmentListResponseDto> recruitments = recruitmentBasics.stream()
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

        return recruitments;
    }

    @Transactional(readOnly = true)
    public List<PopularRecruitmentListResponse> getPopularRecruitments(Category category) {

        List<PopularRecruitmentListResponse> responses = recruitmentRepository.findPopularRecruitments(category);

        return responses;
    }

    private Long countByCondition(SearchCondition searchCondition) {
        return recruitmentRepository.countByCondition(
                searchCondition.keyword(),
                searchCondition.category(),
                searchCondition.subCategory(),
                RecruitmentStatus.OPEN
        );
    }

    public void incrementLikeCount(Long recruitmentId) {
        recruitmentRepository.incrementLikeCount(recruitmentId);
    }

    public void decrementLikeCount(Long recruitmentId) {
        recruitmentRepository.decrementLikeCount(recruitmentId);
    }

    public void incrementReservationCount(Long recruitmentId) {
        recruitmentRepository.incrementReservationCount(recruitmentId);
    }

}
