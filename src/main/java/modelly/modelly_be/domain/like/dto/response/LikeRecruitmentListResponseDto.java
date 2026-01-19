package modelly.modelly_be.domain.like.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import modelly.modelly_be.global.entity.Category;

import java.time.LocalDateTime;
import java.util.List;

public record LikeRecruitmentListResponseDto(
        Long recruitmentLikeId,
        Long recruitmentId,
        String title,
        String designerImage,
        String designerName,
        String recruitmentThumbnail,
        String shop,
        String shopAddress,
        @JsonSerialize(using = Category.CategorySerializer.class)
        Category category,
        //@JsonSerialize(using = SubCategory.SubCategorySerializer.class)
        List<String> subCategories,
        Long reviewCount,
        Double averageRating,
        LocalDateTime createdAt
) {

    public LikeRecruitmentListResponseDto withSubCategories(List<String> subCategories) {
        return new LikeRecruitmentListResponseDto(
                recruitmentLikeId,
                recruitmentId,
                title,
                designerImage,
                designerName,
                recruitmentThumbnail,
                shop,
                shopAddress,
                category,
                subCategories,
                reviewCount,
                averageRating,
                createdAt
        );
    }
}
