package modelly.modelly_be.domain.portfolio.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.portfolio.entity.Portfolio;
import modelly.modelly_be.domain.portfolio.repository.PortfolioRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public void save(Portfolio portfolio) {
        portfolioRepository.save(portfolio);
    }

    public Portfolio getById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_PORTFOLIO));
    }

    public void deletePortfolio(Portfolio portfolio) {
        portfolioRepository.delete(portfolio);
    }
}
