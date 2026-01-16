package modelly.modelly_be.domain.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.notification.service.mapping.ReviewNotificationService;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.domain.review.dto.internal.AverageReview;
import modelly.modelly_be.domain.review.dto.request.ReviewCreateRequestDto;
import modelly.modelly_be.domain.review.dto.request.ReviewUpdateRequestDto;
import modelly.modelly_be.domain.review.dto.response.MyReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewThumbnailListResponseDto;
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
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.listener.dto.S3FolderDeleteEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ModelService modelService;
    private final ReservationService reservationService;
    private final ReplyService replyService;
    private final DesignerService designerService;
    private final ReviewNotificationService reviewNotificationService;
    private final ReviewRepository reviewRepository;
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

        if (existReview(model, reservation)){
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

        reviewRepository.save(review);

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

        //디자이너한테 리뷰 알림 전송
        User designerUser = designer.getUser();
        if (designerUser.getNotificationSetting().isReviewNotification()){
            reviewNotificationService.createReviewNotification(designerUser, model.getNickname(), review.getId());
        }

        return review;
    }

    public boolean existReview(Model model, Reservation reservation) {
        return reviewRepository.existsByModelAndReservation(model, reservation);
    }

    @Transactional
    public Review updateReview(User user, Long reviewId, ReviewUpdateRequestDto requestDto) {

        Model model = modelService.getModelByUser(user);
        Review review = getById(reviewId);

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
        reviewRepository.save(review);

        return review;
    }

    @Transactional
    public void deleteReview(User user, Long reviewId) {

       Model model = modelService.getModelByUser(user);

       Review review = getById(reviewId);

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

        reviewRepository.delete(review);
    }

    public List<MyReviewListResponseDto> getReviewList(User user, Category category, Long cursorId, int size) {
        Model model = modelService.getModelByUser(user);
        log.info("모델 id: "+ model.getId());

        List<MyReviewListResponseDto> responseDtos = reviewRepository.findReviewList(model.getId(), category, cursorId, size);

        return responseDtos;
    }

    public List<ReviewListResponseDto> getDesignerReviewList(Long userId, Long designerId, Long cursorId, int size) {
        Designer designer = designerService.getById(designerId);

        Pageable pageable = PageRequest.of(0, size+1);
        Slice<Review> reviews = reviewRepository.findAllByDesignerAndIdLessThanOrderByCreatedAtDesc(designer, cursorId, pageable);

        Long modelId;
        if (userId != null){
            Model model = modelService.getModelByUserId(userId);
            modelId = model.getId();
        } else {
            modelId = null;
        }



        return reviews.getContent().stream()
                .map(review -> {
                    boolean isMine = false;
                    if ( modelId != null){
                        isMine = review.getModel().getId().equals(modelId);
                    }
                    Reply reply = replyService.getByReview(review)
                            .orElse(null);
                    if (reply != null){
                        return ReviewListResponseDto.of(review, review.getModel().getUser().getImageUrl(),isMine, reply);
                    } else {
                        return ReviewListResponseDto.of(review, review.getModel().getUser().getImageUrl(),isMine);
                    }

                })
                .toList();

    }

    public List<ReviewThumbnailListResponseDto> getReviewThumbnailList(Long designerId, Long cursorId, int size) {
        Designer designer = designerService.getById(designerId);
        Pageable pageable = PageRequest.of(0, size+1);

        Slice<ReviewThumbnailListResponseDto> dtoSlice = reviewRepository.findThumbNailByDesignerAndIdLessThanOrderByCreatedAtDesc(designer, cursorId, pageable);

        return dtoSlice.getContent();
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReview(Long userId, Long reviewId) {
        Review review = getById(reviewId);

        boolean isMine = false;
        if (userId != null){
            Model model = modelService.getModelByUserId(userId);

            isMine = review.getModel().getId().equals(model.getId());
        }

        return ReviewResponseDto.of(review, isMine);
    }

    public AverageReview calculateRating(Designer designer){
        AverageReview result = reviewRepository.findAverageRatingByDesigner(designer);

        if (result == null || result.totalCount() == 0) {
            return new AverageReview(0L, 0.0);
        }

        return result;
    }

    public Review getById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_REVIEW));
    }

    public void isReviewAuthor(Model model, Review review) {
        if (!review.getModel().equals(model)){
            throw new GeneralException(ErrorStatus.FORBIDDEN_DELETE_OR_MODIFY_REVIEW);
        }
    }

    public void save(Review review) {
        reviewRepository.save(review);
    }

    public Slice<Review> findAllByDesigner(Designer designer, Long cursorId, Pageable pageable) {
        return reviewRepository.findAllByDesignerAndIdLessThanOrderByCreatedAtDesc(designer, cursorId, pageable);
    }
}
