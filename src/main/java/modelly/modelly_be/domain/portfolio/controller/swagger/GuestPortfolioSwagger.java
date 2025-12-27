package modelly.modelly_be.domain.portfolio.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "게스트(모델) 뷰에서의 포트폴리오 관련 API", description = "포트폴리오 단건 조회, 특정 디자이너의 포트폴리오 리스트 조회")
public interface GuestPortfolioSwagger {

    @Operation(summary = "특정 디자이너의 포트폴리오 리스트 조회 API", description = """
            모델이 특정 디자이너의 포트폴리오 리스트를 조회할 때 사용하는 API입니다. \n
            ## Path variable
            `designerId`: 디자이너 id \n
            
            ## Request Param
            `cursorId` : 다음 포트폴리오 리스트를 가져올 때, 현재 페이지의 nextCursor값을 넣어주시면 됩니다. \n
            `size`: 한 페이지에서 보여질 포트폴리오의 수
            """)
    @GetMapping("/{designerId}/portfolios")
    ApiResponse<ScrollResponse<PortfolioListResponse>> getPortfolios(
            @PathVariable("designerId") Long designerId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "12") int size);

    @Operation(summary = "포트폴리오 단건 조회", description = """
            모델이 특정 포트폴리오를 상세 조회할 때 사용하는 API입니다.
            ### Path variable
            `portfolioId`: 포트폴리오 id \n
            """)
    @GetMapping("/portfolios/{portfolioId}")
    ApiResponse<PortfolioResponse> getPortfolio(@PathVariable Long portfolioId);
}
