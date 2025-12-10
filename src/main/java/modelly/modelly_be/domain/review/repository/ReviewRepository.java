package modelly.modelly_be.domain.review.repository;

import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.user.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByModelAndReservation(Model model, Reservation reservation);

    Review findByModelAndReservation(Model model, Reservation reservation);
}
