package modelly.modelly_be.domain.review.repository;

import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.review.dto.response.MyReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewThumbnailListResponseDto;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByModelAndReservation(Model model, Reservation reservation);

    @Query("SELECT r.id, r.designer.nickname, r.designer.shop, r.designer.addressLine1, r.rating, r.thumbnail, r.content, r.createdAt" +
            " FROM Review r WHERE r.model = :model " +
            " ORDER BY r.createdAt desc LIMIT :size")
    List<MyReviewListResponseDto> findAllByModel(Model model, int size);

    @Query("SELECT r.id, r.designer.nickname, r.designer.shop, r.designer.addressLine1, r.rating, r.thumbnail, r.content, r.createdAt" +
            " FROM Review r WHERE r.model = :model AND (:cursorId IS NULL OR r.id < :cursorId) " +
            " ORDER BY r.createdAt desc ")
    Slice<MyReviewListResponseDto> findAllByModelAndIdLessThanOrderByCreatedAtDesc(Model model, Long cursorId, Pageable pageable);

    @EntityGraph(attributePaths = {"model", "reviewImages", "model.user"})
    @Query("SELECT r FROM Review r " +
            "WHERE r.designer = :designer AND (:cursorId IS NULL OR r.id < :cursorId) " +
            "ORDER BY r.createdAt desc ")
    Slice<Review> findAllByDesignerAndIdLessThanOrderByCreatedAtDesc(Designer designer, Long cursorId, Pageable pageable);

    @Query("SELECT r.id, r.thumbnail" +
            " FROM Review r WHERE r.designer = :designer AND (:cursorId IS NULL OR r.id < :cursorId) " +
            " ORDER BY r.createdAt desc ")
    Slice<ReviewThumbnailListResponseDto> findThumbNailByDesignerAndIdLessThanOrderByCreatedAtDesc(Designer designer, Long cursorId, Pageable pageable);
}
