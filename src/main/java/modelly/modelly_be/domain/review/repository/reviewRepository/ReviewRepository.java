package modelly.modelly_be.domain.review.repository.reviewRepository;

import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.review.dto.internal.AverageReview;
import modelly.modelly_be.domain.review.dto.response.MyReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewThumbnailListResponseDto;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.global.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    boolean existsByModelAndReservation(Model model, Reservation reservation);

    @Query("SELECT r.id, r.designer.nickname, r.designer.shop, r.designer.addressLine1, r.reservation.subCategories, r.rating, r.thumbnail, r.reviewImages, r.content, r.createdAt " +
            " FROM Review r WHERE r.model = :model AND (:cursorId IS NULL OR r.id < :cursorId) AND r.reservation.category = :category " +
            " ORDER BY r.id desc, r.createdAt desc ")
    Slice<MyReviewListResponseDto> findAllByModelAndIdLessThanOrderByCreatedAtDesc(Model model, Category category, Long cursorId, Pageable pageable);

    @EntityGraph(attributePaths = {"model", "reviewImages", "model.user"})
    @Query("SELECT r FROM Review r " +
            "WHERE r.designer = :designer AND (:cursorId IS NULL OR r.id < :cursorId) " +
            "ORDER BY r.isFixed desc, r.id desc, r.createdAt desc ")
    Slice<Review> findAllByDesignerAndIdLessThanOrderByCreatedAtDesc(Designer designer, Long cursorId, Pageable pageable);

    @Query("SELECT r.id, r.thumbnail, r.isFixed " +
            " FROM Review r WHERE r.designer = :designer AND (:cursorId IS NULL OR r.id < :cursorId) " +
            " ORDER BY r.isFixed desc, r.id desc, r.createdAt desc ")
    Slice<ReviewThumbnailListResponseDto> findThumbNailByDesignerAndIdLessThanOrderByCreatedAtDesc(Designer designer, Long cursorId, Pageable pageable);

    @Query("SELECT COUNT(r), AVG(r.rating) " +
            "FROM Review r " +
            "WHERE r.designer = :designer")
    AverageReview findAverageRatingByDesigner(Designer designer);

}
