package modelly.modelly_be.domain.review.repository.reviewRepository;

import modelly.modelly_be.domain.review.dto.response.MyReviewListResponseDto;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

public interface ReviewRepositoryCustom {
    List<MyReviewListResponseDto> findReviewList(Long modelId, Category category, Long cursorId, int size);
}
