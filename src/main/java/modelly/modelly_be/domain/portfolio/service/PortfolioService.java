package modelly.modelly_be.domain.portfolio.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse;
import modelly.modelly_be.domain.portfolio.entity.Portfolio;
import modelly.modelly_be.domain.portfolio.repository.PortfolioRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    @Transactional
    public void save(Portfolio portfolio) {
        portfolioRepository.save(portfolio);
    }

    public Portfolio getById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_PORTFOLIO));
    }

    public Portfolio getByIdWithDetails(Long portfolioId) {
        return portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_PORTFOLIO));
    }

    @Transactional
    public void deletePortfolio(Portfolio portfolio) {
        portfolioRepository.delete(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> getAllPortfolios(Designer designer, Pageable pageable, Long cursorId) {
        return portfolioRepository.findAllByDesigner(designer, pageable, cursorId);
    }

    @Transactional(readOnly = true)
    public Long countByDesigner(Designer designer) {
        return portfolioRepository.countByDesigner(designer);
    }
}
