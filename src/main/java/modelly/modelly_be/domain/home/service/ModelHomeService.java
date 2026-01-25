package modelly.modelly_be.domain.home.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.home.dto.response.ModelHomeReservationResponse;
import modelly.modelly_be.domain.home.dto.response.PopularRecruitmentListResponse;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.utils.Coordinate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelHomeService {

    private final ReservationService reservationService;
    private final ModelService modelService;
    private final RecruitmentService recruitmentService;
    private final DesignerService designerService;

    //다가오는 확정된 예약들 조회
    @Transactional(readOnly = true)
    public List<ModelHomeReservationResponse> getModelReservationsAtHome(User user) {

        Model model = modelService.getModelByUser(user);

        List<Reservation> reservationList = reservationService.getReservationTop5(model);

        reservationList.forEach(r ->
                r.getSubCategories().size()
        );

        return ModelHomeReservationResponse.from(reservationList);
    }

    //내 주위 모집글 조회
    @Transactional(readOnly = true)
    public List<RecruitmentListResponseDto> getRecruitmentsNearby(Long userId, Category category, Coordinate coordinate) {

        List<RecruitmentListResponseDto> responseDtos = recruitmentService.getNearByRecruitments(userId, coordinate, category);

        return responseDtos;
    }

    //실시간 인기 TOP 모집글 조회
    @Transactional(readOnly = true)
    public List<PopularRecruitmentListResponse> getPopularRecruitments(Category category) {

        List<PopularRecruitmentListResponse> responseDtos = recruitmentService.getPopularRecruitments(category);

        return responseDtos;
    }

    //시술별 인기 디자이너 조회
    @Transactional(readOnly = true)
    public List<DesignerListResponseDto> getPopularDesigners(Long userId, Category category, Coordinate coordinate) {

        List<DesignerListResponseDto> responseDtos = designerService.getPopularDesigners(userId, category, coordinate);

        return responseDtos;
    }

}
