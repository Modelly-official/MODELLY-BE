package modelly.modelly_be.domain.review.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.review.dto.request.ReviewCreateRequestDto;
import modelly.modelly_be.domain.review.dto.request.ReviewUpdateRequestDto;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.entity.ReviewImage;
import modelly.modelly_be.domain.review.repository.ReviewRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ModelService modelService;
    private final ReservationService reservationService;
    private final ReviewRepository reviewRepository;

    @Transactional
    public Review createReview(User user, Long reservationId, ReviewCreateRequestDto requestDto) {
        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        Reservation reservation = reservationService.getById(reservationId);
        Designer designer = reservation.getDesigner();

        if (existReview(model, reservation)){
            throw new GeneralException(ErrorStatus.REVIEW_ALREADY_EXIST);
        }

        Review review = Review.builder()
                .content(requestDto.content())
                .rating(requestDto.rating())
                .designer(designer)
                .reservation(reservation)
                .model(model)
                .isFixed(false)
                //.summary() //기획한테 확인받은 후 수정
                .build();

        reviewRepository.save(review);

        //리뷰 이미지 추가
        if (requestDto.imageUrlList() != null){
            for (String imageUrl : requestDto.imageUrlList()){
                ReviewImage reviewImage = ReviewImage.builder()
                        .review(review)
                        .imageUrl(imageUrl)
                        .build();

                review.addReviewImage(reviewImage);
            }
        }

        return review;
    }

    public boolean existReview(Model model, Reservation reservation) {
        return reviewRepository.existsByModelAndReservation(model, reservation);
    }

    public Review updateReview(User user, Long reviewId, ReviewUpdateRequestDto requestDto) {
        modelService.checkModel(user);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_REVIEW));

        review.update(requestDto);
        reviewRepository.save(review);

        //기존에 있던 리뷰 이미지들 삭제 후, 다시 생성
        if (requestDto.imageUrlList() != null){
            review.getReviewImages().clear();

            for (String imageUrl : requestDto.imageUrlList()){
                ReviewImage reviewImage = ReviewImage.builder()
                        .imageUrl(imageUrl)
                        .review(review)
                        .build();

                review.addReviewImage(reviewImage);
            }
        }

        return review;
    }

}
