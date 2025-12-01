package modelly.modelly_be.domain.recruitment.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.like.service.RecruitmentLikeService;
import modelly.modelly_be.domain.recruitment.dto.response.GuestRecruitmentResponseDto;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.entity.Recruitment;
import modelly.modelly_be.domain.recruitment.repository.recruitmentRepository.RecruitmentRepository;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.user.dto.DesignerResponseDto;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.SortOption;
import modelly.modelly_be.global.utils.SearchCondition;
import modelly.modelly_be.global.utils.UserCoordinate;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public Slice<RecruitmentListResponseDto> getRecruitmensList(Long userId, SearchCondition searchCondition, SortOption sortOption, Long cursorId, int size, UserCoordinate userCoordinate) {
        Slice<RecruitmentListResponseDto> recruitmentListResponseDtoList = recruitmentRepository.findRecruitmentsByConditions(userId,searchCondition,sortOption,cursorId,size,userCoordinate);

        return recruitmentListResponseDtoList;
    }
}
