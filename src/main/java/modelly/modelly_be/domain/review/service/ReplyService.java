package modelly.modelly_be.domain.review.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.repository.ReplyRepository;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;

    public void  delete(Reply reply) {
        replyRepository.delete(reply);
    }

    public Optional<Reply> getByReview(Review review) {
        return replyRepository.findByReview(review);
    }

    @Transactional
    public Reply createReply(Designer designer, Review review, @NotNull(message = "답글 내용은 필수입니다.") String content) {

        Reply reply = Reply.builder()
                .content(content)
                .designer(designer)
                .review(review)
                .build();

        return replyRepository.save(reply);
    }

    public Reply getById(Long replyId) {
        return replyRepository.findById(replyId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_REPLY));
    }

    public Reply updateReply(Reply reply, String content) {
        reply.update(content);

        return replyRepository.save(reply);
    }

    public boolean existAlready(Designer designer, Review review) {
        return replyRepository.existsByDesignerAndReview(designer, review);
    }
}
