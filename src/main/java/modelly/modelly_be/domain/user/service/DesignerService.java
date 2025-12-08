package modelly.modelly_be.domain.user.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.dto.common.CursorInformation;
import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.repository.designerRepository.DesignerRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.SortOption;
import modelly.modelly_be.global.utils.Coordinate;
import modelly.modelly_be.global.utils.SearchCondition;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignerService {
    private final DesignerRepository designerRepository;

    public Designer getByUser(User user) {
        return designerRepository.findByUser(user)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_DESIGNER));
    }

    public List<DesignerListResponseDto> getDesignerList(Long userId, SearchCondition searchCondition, SortOption sortOption, CursorInformation cursorInformation, int size, Coordinate userCoordinate) {
        List<DesignerListResponseDto> designerListResponseDtoList;
        Long cursorId = cursorInformation.cursorId() == null || cursorInformation.cursorId() == 0 ? null : cursorInformation.cursorId();

        switch (sortOption) {
            case NEWEST:
                designerListResponseDtoList = designerRepository.findDesignersByCreatedAt(userId,searchCondition, cursorId, size);
                break;
            case MOST_REVIEWS:
                Long cursorReviewCount = cursorInformation.cursorReviewCount();
                designerListResponseDtoList = designerRepository.findDesignersByReviews(userId,searchCondition,cursorId,cursorReviewCount,size);
                break;
            case DISTANCE:
                Double cursorDistance = cursorInformation.cursorDistance();
                designerListResponseDtoList = designerRepository.findDesignersByDistance(userId, searchCondition, cursorId, cursorDistance, size, userCoordinate);
                break;
            default:
                designerListResponseDtoList = designerRepository.findDesignersByCreatedAt(userId,searchCondition, cursorId, size);
                break;

        }

        return designerListResponseDtoList;
    }

    public Designer getById(Long designerId) {
        return designerRepository.findById(designerId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_DESIGNER));
    }
}
