package modelly.modelly_be.domain.portfolio.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.portfolio.controller.swagger.GuestPortfolioSwagger;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioResponse;
import modelly.modelly_be.domain.portfolio.entity.Portfolio;
import modelly.modelly_be.domain.portfolio.service.GuestPortfolioService;
import modelly.modelly_be.domain.portfolio.service.PortfolioService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GuestPortfolioController implements GuestPortfolioSwagger {

    private final GuestPortfolioService guestPortfolioService;
    private final PortfolioService portfolioService;

    //특정 디자이너의 포트폴리오 리스트 조회
    @Override
    public ApiResponse<ScrollResponse<PortfolioListResponse>> getPortfolios(Long designerId, Long cursorId, int size) {

        ScrollResponse<PortfolioListResponse> response = guestPortfolioService.getDesignerPortfolios(designerId, cursorId, size);

        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<PortfolioResponse> getPortfolio(Long portfolioId) {

        Portfolio portfolio = portfolioService.getByIdWithDetails(portfolioId);

        PortfolioResponse response = PortfolioResponse.from(portfolio);

        return ApiResponse.onSuccess(response);
    }


}
