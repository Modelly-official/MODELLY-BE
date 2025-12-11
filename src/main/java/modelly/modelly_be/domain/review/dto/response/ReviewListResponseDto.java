package modelly.modelly_be.domain.review.dto.response;

import java.time.LocalDateTime;

public record ReviewListResponseDto(
        Long reviewId,
        String designerName,
        String shop,
        String shopAddress,
        float rating,
        String thumbnail,
        String content,
        LocalDateTime createdAt
) {
}
