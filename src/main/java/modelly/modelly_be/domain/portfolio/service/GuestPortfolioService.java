package modelly.modelly_be.domain.portfolio.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.service.DesignerService;
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
    public List<PortfolioListResponse> getDesignerPortfolios(Long designerId, Long cursorId, int size) {
        Designer designer = designerService.getById(designerId);

        Pageable pageable = PageRequest.of(0, size+1);

        return portfolioService.getAllPortfolios(designer, pageable, cursorId);
    }
}
