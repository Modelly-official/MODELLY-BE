package modelly.modelly_be.domain.review.dto.response;


import java.util.List;

public record ReviewListScrollResponse<T>(
        List<T> items,
        boolean hasNext,
        Long nextCursor,
        Boolean nextCursorFixed,
        Long totalCount
) {
    public static<T> ReviewListScrollResponse<T> of(List<T> dtoList, Long totalCount, int size) {
        boolean hasNext = dtoList.size() > size;
        Long nextCursor = 0L;
        Boolean nextCursorFixed = null;

        if (hasNext) {
            T last = dtoList.get(dtoList.size() - 2);

            if (last instanceof ReviewImageListResponse){
                nextCursor = ((ReviewImageListResponse) last).reviewImageId();
                nextCursorFixed = ((ReviewImageListResponse) last).isFixed();
            } else if (last instanceof ReviewListResponseDto) {
                nextCursor = ((ReviewListResponseDto) last).reviewId();
                nextCursorFixed = ((ReviewListResponseDto) last).isFixed();
            }

            dtoList = dtoList.subList(0, size);
        }

        return new ReviewListScrollResponse(dtoList, hasNext, nextCursor, nextCursorFixed, totalCount);

    }
}
