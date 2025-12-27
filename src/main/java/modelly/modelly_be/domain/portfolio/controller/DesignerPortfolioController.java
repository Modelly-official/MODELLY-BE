package modelly.modelly_be.domain.portfolio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.portfolio.dto.request.PortfolioRequest;
import modelly.modelly_be.domain.portfolio.controller.swagger.DesignerPortfolioSwagger;
import modelly.modelly_be.domain.portfolio.dto.request.UpdatePortfolioRequest;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse;
import modelly.modelly_be.domain.portfolio.service.DesignerPortfolioService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DesignerPortfolioController implements DesignerPortfolioSwagger {

    private final DesignerPortfolioService designerPortfolioService;

    //포트폴리오 생성
    public ApiResponse<String> createPortfolio(@AuthenticationPrincipal AuthDetails authDetails, @RequestBody @Valid PortfolioRequest portfolioRequest) {
        designerPortfolioService.createPortfolio(authDetails.user(), portfolioRequest);

        return ApiResponse.onSuccess("포트폴리오 생성이 완료되었습니다.");
    }

    //포트폴리오 수정
    @Override
    public ApiResponse<String> updatePortfolio(AuthDetails authDetails, UpdatePortfolioRequest updatePortfolioRequest, Long portfolioId) {
        designerPortfolioService.updatePortfolio(authDetails.user(), updatePortfolioRequest, portfolioId);

        return ApiResponse.onSuccess("포트폴리오 수정이 완료되었습니다.");
    }

    //포트폴리오 삭제
    @Override
    public ApiResponse<String> deletePortfolio(AuthDetails authDetails, Long portfolioId) {
        designerPortfolioService.deletePortfolio(authDetails.user(), portfolioId);

        return ApiResponse.onSuccess("포트폴리오 삭제가 완료되었습니다.");

    }

    //포트폴리오 리스트 조회
    @Override
    public ApiResponse<ScrollResponse<PortfolioListResponse>> getMyPortfolios(AuthDetails authDetails, Long cursorId, int size) {
        List<PortfolioListResponse> portfolioList = designerPortfolioService.getMyPortfolios(authDetails.user(), cursorId, size);

        ScrollResponse<PortfolioListResponse> response = ScrollUtil.paginate(portfolioList, size);

        return ApiResponse.onSuccess(response);
    }

}
