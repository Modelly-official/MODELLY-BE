package modelly.modelly_be.domain.review.repository;

import modelly.modelly_be.domain.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    // 해당 디자이너의 ReviewImage 삭제
    @Modifying
    @Query("""
        delete from ReviewImage ri
        where ri.review.id in (
            select r.id
            from Review r
            where r.designer.id = :designerId
        )
    """)
    void deleteByDesignerId(@Param("designerId") Long designerId);
}
