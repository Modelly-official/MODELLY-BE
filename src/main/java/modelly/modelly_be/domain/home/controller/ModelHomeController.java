package modelly.modelly_be.domain.home.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.home.controller.swagger.ModelHomeSwagger;
import modelly.modelly_be.domain.home.dto.response.ModelHomeReservationResponse;
import modelly.modelly_be.domain.home.dto.response.PopularRecruitmentListResponse;
import modelly.modelly_be.domain.home.service.ModelHomeService;
import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.Coordinate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ModelHomeController implements ModelHomeSwagger {

    private final ModelHomeService modelHomeService;

    //예약 내역 조회
    @Override
    public ApiResponse<List<ModelHomeReservationResponse>> getModelReservationsAtHome(AuthDetails authDetails) {

        List<ModelHomeReservationResponse> response = modelHomeService.getModelReservationsAtHome(authDetails.user());

        return ApiResponse.onSuccess(response);
    }

    //내 주위 공고 조회
    @Override
    public ApiResponse<List<RecruitmentListResponseDto>> getRecruitmentsNearby(AuthDetails authDetails, Category category, Double userLatitude, Double userLongitude) {
        Long userId = (authDetails != null ? authDetails.user().getId() : null);
        Coordinate coordinate = new Coordinate(userLatitude, userLongitude);

        List<RecruitmentListResponseDto> response = modelHomeService.getRecruitmentsNearby(userId, category, coordinate);

        return ApiResponse.onSuccess(response);
    }

    //실시간 인기 TOP 공고 조회
    @Override
    public ApiResponse<List<PopularRecruitmentListResponse>> getPopularRecruitments(Category category) {
        List<PopularRecruitmentListResponse> response = modelHomeService.getPopularRecruitments(category);

        return ApiResponse.onSuccess(response);
    }

    //시술별 인기 디자이너 조회
    @Override
    public ApiResponse<List<DesignerListResponseDto>> getPopularDesigners(AuthDetails authDetails, Category category, Double userLatitude, Double userLongitude) {
        Long userId = (authDetails != null ? authDetails.user().getId() : null);
        Coordinate coordinate = new Coordinate(userLatitude, userLongitude);

        List<DesignerListResponseDto> response = modelHomeService.getPopularDesigners(userId, category, coordinate);

        return ApiResponse.onSuccess(response);
    }
}
