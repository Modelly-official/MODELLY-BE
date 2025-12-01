package modelly.modelly_be.global.utils;

import modelly.modelly_be.domain.recruitment.dto.response.RecruitmentListResponseDto;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;

import java.util.List;

public class ScrollUtil {
    public static <T> ScrollResponse<T> paginate(List<T> list, int size){
        boolean hasNext = list.size() > size;
        Long nextCursor = 0L;

        if (hasNext) {
            T last = list.get(list.size()-2);

            if (last instanceof RecruitmentListResponseDto){
                nextCursor = ((RecruitmentListResponseDto) last).recruitmentId();
            } else if (last instanceof Designer) {
                nextCursor = ((Designer) last).getId();
            }

            else {
                throw new GeneralException(ErrorStatus.SCROLL_ERROR);
            }

            list = list.subList(0, size);
        }

        return new ScrollResponse<>(list,hasNext,nextCursor);
    }
}
