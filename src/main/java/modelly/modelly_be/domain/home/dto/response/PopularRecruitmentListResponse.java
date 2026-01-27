package modelly.modelly_be.domain.home.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

public record PopularRecruitmentListResponse(
        Long recruitmentId,
        String designerNickname,
        String shop,
        String recruitmentTitle,
        String thumbnailUrl,
        @JsonSerialize(using = Category.CategorySerializer.class)
        Category category,
        List<String> subCategories,
        @JsonIgnore
        Double popularityScore
) {
}
