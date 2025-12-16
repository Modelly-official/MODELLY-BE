package modelly.modelly_be.domain.review.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.dto.request.ReplyRequestDto;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignerReviewService {

    private final ReplyService replyService;
    private final DesignerService designerService;
    private final ReviewService reviewService;

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


    public Reply createReply(User user, Long reviewId, ReplyRequestDto requestDto) {
        Designer designer = designerService.getByUser(user);
        Review review = reviewService.getById(reviewId);

        if (replyService.existAlready(designer, review)) {
            throw new GeneralException(ErrorStatus.REPLY_ALREADY_EXIST);
        }

        checkReviewDesigner(designer, review);

        Reply reply = replyService.createReply(designer, review, requestDto.content());

        return reply;
    }

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
}
