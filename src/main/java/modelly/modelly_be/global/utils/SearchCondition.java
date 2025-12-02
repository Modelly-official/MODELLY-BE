package modelly.modelly_be.global.utils;

import modelly.modelly_be.domain.recruitment.entity.SubCategory;
import modelly.modelly_be.global.entity.Category;

public record SearchCondition(
        Category category,
        SubCategory subCategory,
        String keyword) {
}
