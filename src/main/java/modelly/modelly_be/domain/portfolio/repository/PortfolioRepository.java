package modelly.modelly_be.domain.portfolio.repository;

import modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse;
import modelly.modelly_be.domain.portfolio.entity.Portfolio;
import modelly.modelly_be.domain.user.entity.Designer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    @Query("SELECT new modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse(p.id, p.thumbnail) " +
            "FROM Portfolio p " +
            "WHERE p.designer = :designer AND (:cursorId IS NULL OR p.id < :cursorId) " +
            "ORDER BY p.createdAt desc , p.id desc ")
    List<PortfolioListResponse> findAllByDesigner(Designer designer, Pageable pageable, Long cursorId);

    @Query("SELECT p " +
            "FROM Portfolio p " +
            "LEFT JOIN FETCH p.subCategoryList " +
            "LEFT JOIN FETCH p.portfolioImages " +
            "WHERE p.id = :portfolioId")
    Optional<Portfolio> findByPortfolioId(Long portfolioId);

    Long countByDesigner(Designer designer);

    // 해당 디자이너의 포트폴리오 조회
    List<Portfolio> findAllByDesignerId(Long designerId);
}
