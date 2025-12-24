package modelly.modelly_be.domain.recruitment.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.service.RecruitmentLikeService;
import modelly.modelly_be.domain.recruitment.dto.internal.CursorInformation;
import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentBasic;
import modelly.modelly_be.domain.recruitment.dto.response.DesignerRecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.GuestRecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.entity.enums.SubCategory;
import modelly.modelly_be.domain.recruitment.repository.recruitmentRepository.RecruitmentRepository;
import modelly.modelly_be.domain.reservation.service.ReservationService;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentLikeService recruitmentLikeService;
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
        //관련된 찜 삭제
        recruitmentLikeService.deleteRecruitmentLike(recruitment);

        //관련된 예약 연관관계 삭제
        reservationService.deleteRelationshipWithRecruitment(recruitment);

        recruitmentRepository.delete(recruitment);
    }


    @Transactional(readOnly = true)
    public GuestRecruitmentResponseDto getByIdWithDesigner(Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findByIdWithAllDetails(recruitmentId);
        return GuestRecruitmentResponseDto.of(
                DesignerResponseDto.from(recruitment.getDesigner()),
                recruitment
        );
    }

    public List<RecruitmentListResponseDto> getRecruitmensList(Long userId, SearchCondition searchCondition, SortOption sortOption, CursorInformation cursorInformation, int size, Coordinate userCoordinate) {
        List<RecruitmentBasic> recruitmentBasics;
        Long cursorId = cursorInformation.cursorId() == null || cursorInformation.cursorId() == 0 ? null : cursorInformation.cursorId();

        switch (sortOption){
            case NEWEST:
                recruitmentBasics = recruitmentRepository.findRecruitmentsByCreatedAt(userId,searchCondition, cursorId,size);
                break;
            case MOST_REVIEWS:
                Long cursorReviewCount = cursorInformation.cursorReviewCount() == null ? null : cursorInformation.cursorReviewCount();
                recruitmentBasics = recruitmentRepository.findRecruitmentsByReviews(userId,searchCondition, cursorId, cursorReviewCount,size);
                break;
            case DISTANCE:
                Double cursorDistance = cursorInformation.cursorDistance() == null ? null : cursorInformation.cursorDistance();
                recruitmentBasics = recruitmentRepository.findRecruitmentsByDistance(userId,searchCondition, cursorId, cursorDistance,size, userCoordinate);
                break;
            default:
               recruitmentBasics = recruitmentRepository.findRecruitmentsByCreatedAt(userId,searchCondition, cursorId,size);
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
                            basic.distance(),
                            basic.isLiked(),
                            basic.createdAt()
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

}
