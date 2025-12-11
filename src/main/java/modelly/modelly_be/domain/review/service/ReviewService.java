package modelly.modelly_be.domain.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.review.dto.request.ReviewCreateRequestDto;
import modelly.modelly_be.domain.review.dto.request.ReviewUpdateRequestDto;
import modelly.modelly_be.domain.review.dto.response.ReviewListResponseDto;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.entity.ReviewImage;
import modelly.modelly_be.domain.review.repository.ReviewRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ModelService modelService;
    private final ReservationService reservationService;
    private final ReplyService replyService;
    private final ReviewRepository reviewRepository;

    @Transactional
    public Review createReview(User user, Long reservationId, ReviewCreateRequestDto requestDto) {
        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        Reservation reservation = reservationService.getById(reservationId);
        Designer designer = reservation.getDesigner();

        //완료된 예약에 대해서만 작성가능하도록 체크
        if (reservation.getEndTime().isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")))){
            throw new GeneralException(ErrorStatus.RESERVATION_NOT_COMPLETED);
        }

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

    @Transactional
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

    @Transactional
    public void deleteReview(User user, Long reviewId) {
        modelService.checkModel(user);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_REVIEW));

       Reply reply = replyService.getByReview(review)
                       .orElse(null);

       if (reply != null){
           replyService.delete(reply);
       }

        reviewRepository.delete(review);
    }

    public List<ReviewListResponseDto> getReviewList(User user, Long cursorId, int size) {
        modelService.checkModel(user);
        Model model = modelService.getModelByUser(user);

        Slice<ReviewListResponseDto> responseDtos;
        Pageable pageable = PageRequest.of(0, size+1);

        //if (cursorId != null){
            responseDtos = reviewRepository.findAllByModelAndIdLessThanOrderByCreatedAtDesc(model, cursorId, pageable);
//        } else {
//            responseDtos = reviewRepository.findAllByModel(model, size+1);
//        }

        return responseDtos.getContent();
    }
}
