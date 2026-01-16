package modelly.modelly_be.domain.review.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.service.mapping.ReviewNotificationService;
import modelly.modelly_be.domain.review.dto.request.ReplyRequestDto;
import modelly.modelly_be.domain.review.dto.response.DesignerReviewListResponseDto;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignerReviewService {

    private final ReplyService replyService;
    private final DesignerService designerService;
    private final ReviewService reviewService;
    private final ReviewNotificationService reviewNotificationService;

    @Transactional
    public void fixReview(User user, Long reviewId){
        Designer designer = designerService.getByUser(user);

        Review review = reviewService.getById(reviewId);

        checkReviewDesigner(designer, review);

        if (review.isFixed()){
            review.updateFixStatus(false);
        } else {
            review.updateFixStatus(true);
        }

        reviewService.save(review);
    }

    private void checkReviewDesigner(Designer designer, Review review) {
        if (!review.getDesigner().getId().equals(designer.getId())) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
    }


    @Transactional
    public Reply createReply(User user, Long reviewId, ReplyRequestDto requestDto) {
        Designer designer = designerService.getByUser(user);
        Review review = reviewService.getById(reviewId);

        if (replyService.existAlready(designer, review)) {
            throw new GeneralException(ErrorStatus.REPLY_ALREADY_EXIST);
        }

        checkReviewDesigner(designer, review);

        Reply reply = replyService.createReply(designer, review, requestDto.content());

        User model = review.getModel().getUser();

        if (model.getNotificationSetting().isReviewNotification()){
            reviewNotificationService.createReplyNotification(model, designer.getNickname(), reviewId);
        }

        return reply;
    }

    @Transactional
    public Reply updateReply(User user, Long replyId, ReplyRequestDto requestDto) {
        Designer designer = designerService.getByUser(user);
        Reply reply = replyService.getById(replyId);

        checkReplyOwner(designer, reply);

        Reply modifiedReply = replyService.updateReply(reply, requestDto.content());

        return modifiedReply;
    }

    private void checkReplyOwner(Designer designer, Reply reply) {
        if (!reply.getDesigner().getId().equals(designer.getId())) {
            throw new GeneralException(ErrorStatus.FORBIDDEN_MODIFY_REPLY);
        }
    }

    public List<DesignerReviewListResponseDto> getDesignerReviewList(User user, Long cursorId, int size) {
        Designer designer = designerService.getByUser(user);

        Pageable pageable = PageRequest.of(0, size+1);
        Slice<Review> reviews = reviewService.findAllByDesigner(designer, cursorId, pageable);

        return reviews.getContent().stream()
                .map(review -> {
                    Reply reply = replyService.getByReview(review)
                            .orElse(null);

                    if (reply != null) {
                        return DesignerReviewListResponseDto.of(review, review.getModel().getUser().getImageUrl(), reply);
                    } else {
                        return DesignerReviewListResponseDto.of(review, review.getModel().getUser().getImageUrl());
                    }
                })
                .toList();
    }
}
