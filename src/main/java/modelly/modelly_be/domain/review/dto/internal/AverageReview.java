package modelly.modelly_be.domain.review.dto.internal;

import lombok.Builder;

@Builder
public record AverageReview(
        Long totalCount,
        Double averageRating
) {
}
