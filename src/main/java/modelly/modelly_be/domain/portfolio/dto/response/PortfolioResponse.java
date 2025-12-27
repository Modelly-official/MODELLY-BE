package modelly.modelly_be.domain.portfolio.dto.response;

import modelly.modelly_be.domain.portfolio.entity.Portfolio;
import modelly.modelly_be.domain.portfolio.entity.PortfolioImage;
import modelly.modelly_be.domain.recruitment.entity.enums.SubCategory;

import java.util.List;

public record PortfolioResponse(
        Long portfolioId,
        String title,
        List<String> subCategoryList,
        String content,
        List<String> imageList
) {

    public static PortfolioResponse from(Portfolio portfolio) {
        List<String> subCategoryList = portfolio.getSubCategoryList().stream()
                .map(SubCategory::getDescription)
                .toList();

        List<String> imageList = portfolio.getPortfolioImages().stream()
                .map(PortfolioImage::getImageUrl)
                .toList();

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                subCategoryList,
                portfolio.getContent(),
                imageList
        );
    }
}
