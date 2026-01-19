package modelly.modelly_be.domain.like.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import modelly.modelly_be.global.entity.SubCategory;

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
        String category,
        @JsonSerialize(using = SubCategory.SubCategorySerializer.class)
        List<SubCategory> subCategories,
        Long reviewCount,
        Double averageRating,
        LocalDateTime createdAt
) {

    public LikeRecruitmentListResponseDto withSubCategories(List<SubCategory> subCategories) {
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
