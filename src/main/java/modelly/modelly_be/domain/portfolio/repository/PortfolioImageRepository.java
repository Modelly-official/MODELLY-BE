package modelly.modelly_be.domain.portfolio.repository;

import modelly.modelly_be.domain.portfolio.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
    // 해당 디자이너의 포트폴리오 이미지 삭제
    @Modifying
    @Query("""
        delete from PortfolioImage pi
        where pi.portfolio.designer.id = :designerId
    """)
    void deleteByDesignerId(Long designerId);
}
