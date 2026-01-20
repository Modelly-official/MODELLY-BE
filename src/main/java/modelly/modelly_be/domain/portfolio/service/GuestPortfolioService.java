package modelly.modelly_be.domain.portfolio.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestPortfolioService {

    private final PortfolioService portfolioService;
    private final DesignerService designerService;

    @Transactional(readOnly = true)
    public ScrollResponse<PortfolioListResponse> getDesignerPortfolios(Long designerId, Long cursorId, int size) {
        Designer designer = designerService.getById(designerId);

        Pageable pageable = PageRequest.of(0, size+1);

        List<PortfolioListResponse> portfolioList = portfolioService.getAllPortfolios(designer, pageable, cursorId);

        Long totalCount = portfolioService.countByDesigner(designer);

        ScrollResponse<PortfolioListResponse> response = ScrollUtil.paginate(portfolioList, size, totalCount);

        return response;
    }
}
