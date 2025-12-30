package modelly.modelly_be.global.utils;

import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.global.entity.Category;

public record SearchCondition(
        Category category,
        SubCategory subCategory,
        String keyword) {
}
