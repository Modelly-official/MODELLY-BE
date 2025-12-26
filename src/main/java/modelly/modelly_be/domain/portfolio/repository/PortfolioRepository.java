package modelly.modelly_be.domain.portfolio.repository;

import modelly.modelly_be.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
