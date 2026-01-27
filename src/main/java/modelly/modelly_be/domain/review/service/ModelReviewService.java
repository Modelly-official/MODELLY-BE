package modelly.modelly_be.domain.review.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.event.dto.review.ReviewCreateEvent;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.review.dto.request.ReviewCreateRequestDto;
import modelly.modelly_be.domain.review.dto.request.ReviewUpdateRequestDto;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.entity.ReviewImage;
import modelly.modelly_be.domain.review.repository.reviewRepository.ReviewRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.listener.dto.S3FolderDeleteEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModelReviewService {

    private final ModelService modelService;
    private final ReservationService reservationService;
    private final ReplyService replyService;
    private final DesignerService designerService;
    private final ReviewService reviewService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Review createReview(User user, Long reservationId, ReviewCreateRequestDto requestDto) {
        Model model = modelService.getModelByUser(user);

        Reservation reservation = reservationService.getById(reservationId);
        Designer designer = reservation.getDesigner();

        //예약자인지 확인
        if (!reservation.getModel().equals(model)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN_CREATE_REVIEW);
        }

        //완료된 예약에 대해서만 작성가능하도록 체크
        if (LocalDateTime.of(reservation.getDate(),reservation.getEndTime()).isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")))){
            throw new GeneralException(ErrorStatus.RESERVATION_NOT_COMPLETED);
        }

        if (reviewService.existReview(model, reservation)){
            throw new GeneralException(ErrorStatus.REVIEW_ALREADY_EXIST);
        }

        String summary = reservation.getRecruitment().getSubCategoryList().stream()
                .map(subCategory -> subCategory.getDescription())
                .collect(Collectors.joining(", "));

        Review review = Review.builder()
                .content(requestDto.content())
                .rating(requestDto.rating())
                .designer(designer)
                .reservation(reservation)
                .model(model)
                .isFixed(false)
                .summary(summary)
                .build();

        reviewService.save(review);

        //리뷰 이미지 추가
        if (requestDto.imageUrlList() != null){
            review.updateImageInf(requestDto.imageFolderId(), requestDto.thumbnail());
            for (String imageUrl : requestDto.imageUrlList()){
                ReviewImage reviewImage = ReviewImage.builder()
                        .review(review)
                        .imageUrl(imageUrl)
                        .build();

                review.addReviewImage(reviewImage);
            }
        }

        designerService.incrementReviewCount(designer.getId());

        //디자이너한테 리뷰 알림 전송
        User designerUser = designer.getUser();
        boolean isNotificationOn = designerUser.getNotificationSetting().isReviewNotification();

        eventPublisher.publishEvent(new ReviewCreateEvent(designerUser, model.getNickname(), review.getId(), isNotificationOn));


        return review;
    }

    @Transactional
    public Review updateReview(User user, Long reviewId, ReviewUpdateRequestDto requestDto) {

        Model model = modelService.getModelByUser(user);
        Review review = reviewService.getById(reviewId);

        isReviewAuthor(model, review);

        //기존에 있던 리뷰 이미지들 삭제 후, 다시 생성
        if (requestDto.imageFolderId() != null
                && !requestDto.imageFolderId().equals(review.getImageFolderId())
                && requestDto.thumbnail() !=null){

            //기존 S3 폴더 삭제
            if (review.getImageFolderId() != null) {
                String oldFolderPath = "reviews/" + review.getImageFolderId() + "/";
                eventPublisher.publishEvent(new S3FolderDeleteEvent(oldFolderPath));
            }

            //DB에서 리뷰 이미지 리스트 삭제
            review.getReviewImages().clear();

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

            review.updateImageInf(requestDto.imageFolderId(), requestDto.thumbnail());
        }

        review.update(requestDto);
        reviewService.save(review);

        return review;
    }

    @Transactional
    public void deleteReview(User user, Long reviewId) {

        Model model = modelService.getModelByUser(user);

        Review review = reviewService.getById(reviewId);

        isReviewAuthor(model, review);

        Reply reply = replyService.getByReview(review)
                .orElse(null);

        if (reply != null){
            replyService.delete(reply);
        }

        //기존 이미지 삭제
        if (review.getImageFolderId() != null) {
            String oldFolderPath = "reviews/" + review.getImageFolderId() + "/";
            eventPublisher.publishEvent(new S3FolderDeleteEvent(oldFolderPath));
        }

        reviewService.delete(review);

        designerService.decrementReviewCount(review.getDesigner().getId());

    }

    public void isReviewAuthor(Model model, Review review) {
        if (!review.getModel().equals(model)){
            throw new GeneralException(ErrorStatus.FORBIDDEN_DELETE_OR_MODIFY_REVIEW);
        }
    }
}
