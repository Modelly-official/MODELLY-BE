package modelly.modelly_be.global.utils;

import lombok.Builder;

import java.util.List;

@Builder
public record ScrollResponse<T>(
        List<T> items,
        boolean hasNext,
        Long nextCursor,
        Long totalCount
) {
}
