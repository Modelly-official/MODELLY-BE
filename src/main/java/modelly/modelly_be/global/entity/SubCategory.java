package modelly.modelly_be.global.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public enum SubCategory {
    // HAIR
    HAIR_CUT("커트", Category.HAIR),
    HAIR_PERM("파마", Category.HAIR),
    HAIR_COLORING("염색", Category.HAIR),
    HAIR_MAGIC("매직", Category.HAIR),

    // NAIL
    ONE_COLOR("원컬러", Category.NAIL),
    ART("네일아트", Category.NAIL),
    PEDICURE("페디큐어", Category.NAIL),

    // EYELASH
    EYELASH_PERM("펌", Category.EYELASH),
    EYELASH_EXTENSION("연장", Category.EYELASH),

    // TATTOO
    LIP_TATTOO("립", Category.TATTOO),
    EYEBROW_TATTOO("눈썹", Category.TATTOO),
    NORMAL_TATTOO("일반아트", Category.TATTOO),

    ETC("기타", null); // 기타는 상황에 따라 처리

    private final String description;
    private final Category parentCategory;

    SubCategory(String description, Category parentCategory) {
        this.description = description;
        this.parentCategory = parentCategory;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public String getDescription() {
        return description;
    }

    public static class SubCategorySerializer extends JsonSerializer<SubCategory> {

        @Override
        public void serialize(SubCategory value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.getDescription());
            }
        }
    }
}
