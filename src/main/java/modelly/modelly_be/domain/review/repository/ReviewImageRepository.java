package modelly.modelly_be.domain.review.repository;

import modelly.modelly_be.domain.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
}
