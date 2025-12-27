package modelly.modelly_be.domain.portfolio.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.portfolio.dto.request.PortfolioRequest;
import modelly.modelly_be.domain.portfolio.dto.request.UpdatePortfolioRequest;
import modelly.modelly_be.domain.portfolio.dto.response.PortfolioListResponse;
import modelly.modelly_be.domain.portfolio.entity.Portfolio;
import modelly.modelly_be.domain.portfolio.entity.PortfolioImage;
import modelly.modelly_be.domain.recruitment.entity.enums.SubCategory;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.DesignerService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.listener.dto.S3FolderDeleteEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignerPortfolioService {

    private final DesignerService designerService;
    private final PortfolioService portfolioService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createPortfolio(User user, @Valid PortfolioRequest portfolioRequest) {
        Designer designer = designerService.getByUser(user);

        Portfolio portfolio = Portfolio.builder()
                .title(portfolioRequest.title())
                .designer(designer)
                .content(portfolioRequest.content())
                .build();

        if (portfolioRequest.subCategoryList() != null && !portfolioRequest.subCategoryList().isEmpty()) {
            updateSubCategory(portfolio, designer.getCategory(), portfolioRequest.subCategoryList());
        }

        portfolioService.save(portfolio);

        if (portfolioRequest.imageUrls() != null){
            uploadPortfolioImage(portfolio, portfolioRequest.imageUrls(), portfolioRequest.thumbnail(), portfolioRequest.folderId());
        }

    }

    @Transactional
    public void updatePortfolio(User user, UpdatePortfolioRequest request, Long portfolioId) {
        Designer designer = designerService.getByUser(user);

        Portfolio portfolio = portfolioService.getById(portfolioId);

        isPortfolioAuthor(designer, portfolio);

        if (request.subCategoryList() != null && !request.subCategoryList().isEmpty()) {
            portfolio.getSubCategoryList().clear();
            updateSubCategory(portfolio, designer.getCategory(), request.subCategoryList());
        }

        if (request.folderId() != null && !request.folderId().equals(portfolio.getImageFolderId())) {
            if (portfolio.getImageFolderId() != null) {
                String oldFolderPath = "portfolios/" + portfolio.getImageFolderId() +"/";
                eventPublisher.publishEvent(new S3FolderDeleteEvent(oldFolderPath));
            }

            portfolio.getPortfolioImages().clear();
            portfolio.updateImageInf(null, null);

            if (request.imageUrls() != null) {
                uploadPortfolioImage(portfolio, request.imageUrls(), request.thumbnail(), portfolio.getImageFolderId());
            }
        }

        portfolio.updatePortfolio(request.title(), request.content());
        portfolioService.save(portfolio);
    }

    private void uploadPortfolioImage(Portfolio portfolio, List<String> imageUrls, String thumbnail, String folderId){
        portfolio.updateImageInf(folderId, thumbnail);
        for (String imageUrl : imageUrls) {
            if (imageUrl != null) {
                PortfolioImage portfolioImage = PortfolioImage.builder()
                        .portfolio(portfolio)
                        .imageUrl(imageUrl)
                        .build();

                portfolio.addImage(portfolioImage);
            }
        }
    }

    private void updateSubCategory(Portfolio portfolio, Category parentCategory, List<SubCategory> subCategoryList) {

        // 모든 서브 카테고리가 상위 카테고리에 속하는지 검증
        boolean isAllMatch = subCategoryList.stream()
                .allMatch(sub -> sub != SubCategory.ETC? sub.getParentCategory() == parentCategory : true);

        if (!isAllMatch) {
            throw new GeneralException(ErrorStatus.SUBCATEGORY_MISMATCH);
        }

        portfolio.getSubCategoryList()
                .addAll(subCategoryList);
    }

    private void isPortfolioAuthor(Designer designer, Portfolio portfolio) {
        if (!portfolio.getDesigner().getId().equals(designer.getId())) {
            throw new GeneralException(ErrorStatus.FORBIDDEN_MODIFY_OR_DELETE_PORTFOLIO);
        }
    }

    @Transactional
    public void deletePortfolio(User user, Long portfolioId) {
        Designer designer = designerService.getByUser(user);

        Portfolio portfolio = portfolioService.getById(portfolioId);

        isPortfolioAuthor(designer, portfolio);

        if (portfolio.getImageFolderId() != null) {
            String oldFolderPath = "portfolios/" + portfolio.getImageFolderId() +"/";
            eventPublisher.publishEvent(new S3FolderDeleteEvent(oldFolderPath));
        }

        portfolioService.deletePortfolio(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> getMyPortfolios(User user, Long cursorId, int size) {
        Designer designer = designerService.getByUser(user);

        Pageable pageable = PageRequest.of(0, size+1);

        return portfolioService.getAllPortfolios(designer, pageable, cursorId);
    }
}
