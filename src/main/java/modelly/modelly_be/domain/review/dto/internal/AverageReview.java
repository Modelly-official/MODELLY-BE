package modelly.modelly_be.domain.review.dto.internal;

import lombok.Builder;

@Builder
public record AverageReview(
        Integer totalCount,
        Double averageRating
) {
}
