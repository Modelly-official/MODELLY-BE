package modelly.modelly_be.domain.user.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.map.dto.response.ShopResponse;
import modelly.modelly_be.domain.recruitment.dto.internal.CursorInformation;
import modelly.modelly_be.domain.user.dto.response.DesignerListResponseDto;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.domain.user.repository.designerRepository.DesignerRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.Category;
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
                designerListResponseDtoList = designerRepository.findDesignersByCreatedAt(userId,searchCondition, cursorId, size, userCoordinate);
                break;
            case MOST_REVIEWS:
                Long cursorReviewCount = cursorInformation.cursorReviewCount();
                designerListResponseDtoList = designerRepository.findDesignersByReviews(userId,searchCondition,cursorId,cursorReviewCount,size, userCoordinate);
                break;
            case DISTANCE:
                Double cursorDistance = cursorInformation.cursorDistance();
                designerListResponseDtoList = designerRepository.findDesignersByDistance(userId, searchCondition, cursorId, cursorDistance, size, userCoordinate);
                break;
            default:
                designerListResponseDtoList = designerRepository.findDesignersByCreatedAt(userId,searchCondition, cursorId, size, userCoordinate);
                break;

        }

        return designerListResponseDtoList;
    }

    public Designer getById(Long designerId) {
        return designerRepository.findById(designerId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_DESIGNER));
    }

    public void checkDesigner(User user) {
        if (user.getUserRole() != UserRole.DESIGNER) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
    }

    // lock 조회용
    public Designer getByIdForUpdate(Long designerId) {
        Designer designer = designerRepository.findByIdForUpdate(designerId);
        if (designer == null) {
            throw new GeneralException(ErrorStatus.NOT_FOUND_DESIGNER);
        }
        return designer;
    }

    public List<ShopResponse> getShopList(Long userId, Category category, int size, Coordinate coordinate){
        return designerRepository.findShopsByDistance(userId,category,size,coordinate);
    }
}
