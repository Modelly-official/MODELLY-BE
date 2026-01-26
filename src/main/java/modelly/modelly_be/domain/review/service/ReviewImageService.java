package modelly.modelly_be.domain.review.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.review.dto.response.ReviewImageListResponse;
import modelly.modelly_be.domain.review.repository.ReviewImageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewImageService {

    private final ReviewImageRepository reviewImageRepository;

    public Slice<ReviewImageListResponse> findReviewImageByCondition(Long designerId, Long cursorId, Pageable pageable) {
        return reviewImageRepository.findReviewImageByCondition(designerId, cursorId, pageable);
    }

    public Long countByDesignerId(Long designerId) {
        return reviewImageRepository.countByDesignerId(designerId);
    }
}
