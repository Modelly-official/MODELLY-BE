package modelly.modelly_be.domain.recruitment.dto.common;

import jakarta.annotation.Nullable;

public record CursorInformation(
        @Nullable
        Long cursorId,
        @Nullable
        Long cursorReviewCount,
        @Nullable
        Double cursorDistance
) {
}
