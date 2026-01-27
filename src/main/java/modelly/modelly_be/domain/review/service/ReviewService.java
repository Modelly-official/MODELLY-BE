package modelly.modelly_be.domain.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.review.dto.internal.AverageReview;
import modelly.modelly_be.domain.review.dto.response.MyReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewListResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewResponseDto;
import modelly.modelly_be.domain.review.dto.response.ReviewThumbnailListResponseDto;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.repository.reviewRepository.ReviewRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.Model;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.utils.ScrollResponse;
import modelly.modelly_be.global.utils.ScrollUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ModelService modelService;
    private final ReplyService replyService;
    private final DesignerService designerService;
    private final ReviewRepository reviewRepository;

    public boolean existReview(Model model, Reservation reservation) {
        return reviewRepository.existsByModelAndReservation(model, reservation);
    }

    public ScrollResponse<MyReviewListResponseDto> getReviewList(User user, Category category, Long cursorId, int size) {
        Model model = modelService.getModelByUser(user);
        log.info("모델 id: "+ model.getId());

        List<MyReviewListResponseDto> listDtos = reviewRepository.findReviewList(model.getId(), category, cursorId, size);

        Long totalCount = countByModelAndCategory(model, category);

        ScrollResponse<MyReviewListResponseDto> responseDtos = ScrollUtil.paginate(listDtos, size, totalCount);

        return responseDtos;
    }

    @Transactional(readOnly = true)
    public Long countByModelAndCategory(Model model, Category category) {
        return reviewRepository.countByModelAndCategory(model, category);
    }

    public ScrollResponse<ReviewListResponseDto> getDesignerReviewList(Long userId, Long designerId, Long cursorId, int size) {
        Designer designer = designerService.getById(designerId);

        Pageable pageable = PageRequest.of(0, size+1);
        Slice<Review> reviews = reviewRepository.findAllByDesignerAndIdLessThanOrderByCreatedAtDesc(designer, cursorId, pageable);

        Long totalCount = countByDesigner(designer);

        Long modelId;
        if (userId != null){
            Model model = modelService.getModelByUserId(userId);
            modelId = model.getId();
        } else {
            modelId = null;
        }

        List<ReviewListResponseDto> dtolist = reviews.getContent().stream()
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

        ScrollResponse<ReviewListResponseDto> responseDtos = ScrollUtil.paginate(dtolist, size, totalCount);
        return responseDtos;

    }

    public ScrollResponse<ReviewThumbnailListResponseDto> getReviewThumbnailList(Long designerId, Long cursorId, int size) {
        Designer designer = designerService.getById(designerId);
        Pageable pageable = PageRequest.of(0, size+1);

        Slice<ReviewThumbnailListResponseDto> dtoSlice = reviewRepository.findThumbNailByDesignerAndIdLessThanOrderByCreatedAtDesc(designer, cursorId, pageable);

        Long totalCount = countByDesigner(designer);

        ScrollResponse<ReviewThumbnailListResponseDto> responseDtos = ScrollUtil.paginate(dtoSlice.getContent(), size, totalCount);

        return responseDtos;
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

    public void save(Review review) {
        reviewRepository.save(review);
    }

    public void delete(Review review) {
        reviewRepository.delete(review);
    }

    public Slice<Review> findAllByDesigner(Designer designer, Long cursorId, Pageable pageable) {
        return reviewRepository.findAllByDesignerAndIdLessThanOrderByCreatedAtDesc(designer, cursorId, pageable);
    }

    @Transactional(readOnly = true)
    public Long countByDesigner(Designer designer) {
        return reviewRepository.countByDesigner(designer);
    }
}
