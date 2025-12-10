package modelly.modelly_be.domain.review.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.entity.Reply;
import modelly.modelly_be.domain.review.entity.Review;
import modelly.modelly_be.domain.review.repository.ReplyRepository;
import org.springframework.stereotype.Service;

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
}
