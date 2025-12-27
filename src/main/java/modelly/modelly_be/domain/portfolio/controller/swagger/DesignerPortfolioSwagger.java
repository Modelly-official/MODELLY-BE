package modelly.modelly_be.domain.portfolio.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import modelly.modelly_be.domain.portfolio.dto.request.PortfolioRequest;
import modelly.modelly_be.domain.portfolio.dto.request.UpdatePortfolioRequest;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import modelly.modelly_be.global.utils.ScrollResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "디자이너 뷰에서의 포트폴리오 관련 API", description = "포트폴리오 CUD, 내 포트폴리오 리스트 조회")
public interface DesignerPortfolioSwagger {

    @Operation(summary = "포트폴리오 생성 API", description = """
            디자이너가 포트폴리오를 생성할 때 사용하는 API입니다. \n
            ### Request Param </p>
            `title`: 포트폴리오 제목 \n
            `thumbnail`: 포트폴리오 썸네일 \n
            `folderId`: 포트폴리오 사진이 저장된 폴더 id  \n
            `imageUrls`: 포트폴리오 사진 리스트 \n
            `content`: 시술 상세 내용 \n
            `subCategoryList`: 세부 카테고리 리스트 \n
            """)
    @PostMapping("/designers/portfolios")
    ApiResponse<String> createPortfolio(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestBody @Valid PortfolioRequest portfolioRequest);

    @Operation(summary = "포트폴리오 수정 API",description = """
            디자이너가 포트폴리오를 수정할 때 사용하는 API입니다. \n
            ### Path Variable </p>
            `portfolioId`: 포트폴리오 id \n
            ### Request Param </p>
            `title`: 포트폴리오 제목 \n
            `thumbnail`: 포트폴리오 썸네일 \n
            `folderId`: 포트폴리오 사진이 저장된 폴더 id  \n
            `imageUrls`: 포트폴리오 사진 리스트 \n
            `content`: 시술 상세 내용 \n
            `subCategoryList`: 세부 카테고리 리스트 \n
            """)
    @PutMapping("/designers/portfolios/{portfolioId}")
    ApiResponse<String> updatePortfolio(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestBody @Valid UpdatePortfolioRequest updatePortfolioRequest,
            @PathVariable Long portfolioId);

    @Operation(summary = "포트폴리오 삭제 API", description = """
            디자이너가 포트폴리오를 삭제할 때 사용하는 API입니다. \n
            
            """)
    @DeleteMapping("/designers/portfolios/{portfolioId}")
    ApiResponse<String> deletePortfolio(@AuthenticationPrincipal AuthDetails authDetails, @PathVariable Long portfolioId);

    @Operation(summary = "내 포트폴리오 리스트 조회 API", description = """
            디자이너가 자신의 포트폴리오 리스트를 조회할 때 사용하는 API입니다. \n
            ## Request Param
            `cursorId` : 다음 포트폴리오 리스트를 가져올 때, 현재 페이지의 nextCursor값을 넣어주시면 됩니다. \n
            `size`: 한 페이지에서 보여질 포트폴리오의 수
            """)
    @GetMapping("/designers/portfolios")
    ApiResponse<ScrollResponse<PortfolioListResponse>> getMyPortfolios(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "12") int size);
}
