package modelly.modelly_be.domain.portfolio.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import modelly.modelly_be.global.entity.SubCategory;

import java.util.List;

public record UpdatePortfolioRequest(
        @Schema(description = "포트폴리오 제목")
        @Size(max = 20, message = "포트폴리오 제목은 최대 20자까지만 가능합니다.")
        String title,
        @Schema(description = "포트폴리오 썸네일")
        String thumbnail,
        @Schema(description = "포트폴리오 사진이 저장된 폴더 id. presignedUrl 발급 후, 응답 dto의 folderId을 넣어주세요.")
        String folderId,
        @Schema(description = "포트폴리오 사진 리스트")
        List<String> imageUrls,
        @Schema(description = "시술 상세 내용")
        String content,
        @Schema(description = "세부 카테고리")
        List<SubCategory> subCategoryList
) {
}
