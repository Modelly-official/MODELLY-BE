package modelly.modelly_be.domain.review.repository;

import modelly.modelly_be.domain.review.dto.response.ReviewImageListResponse;
import modelly.modelly_be.domain.review.entity.ReviewImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    @Query("SELECT ri.id, r.id, ri.imageUrl, r.isFixed FROM ReviewImage ri " +
            "JOIN ri.review r " +
            "WHERE r.designer.id = :designerId AND ( (:cursorId IS NULL AND :cursorIsFixed IS NULL) OR " +
            "  (r.isFixed = :cursorIsFixed AND ri.id < :cursorId) OR " +
            "  (r.isFixed = false AND :cursorIsFixed = true)) " +
            "ORDER BY r.isFixed desc, ri.id desc ")
    Slice<ReviewImageListResponse> findReviewImageByCondition(@Param("designerId") Long designerId, @Param("cursorId")Long cursorId, @Param("cursorIsFixed")Boolean cursorIsFixed, Pageable pageable);

    @Query("SELECT COUNT(ri) FROM ReviewImage ri " +
            "WHERE ri.review.designer.id = :designerId")
    Long countByDesignerId(@Param("designerId") Long designerId);
}
