package modelly.modelly_be.domain.review.repository;

import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.user.entity.Designer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    Optional<Reply> findByReview(Review review);

    boolean existsByDesignerAndReview(Designer designer, Review review);

    // 해당 디자이너의 답글 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Reply r WHERE r.designer.id = :designerId")
    void deleteByDesignerId(@Param("designerId") Long designerId);
}
