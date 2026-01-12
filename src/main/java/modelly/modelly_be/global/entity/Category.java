package modelly.modelly_be.global.entity;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public enum Category {
    HAIR("헤어"),
    NAIL("네일"),
    TATTOO("타투"),
    EYELASH("속눈썹")
    ;

    private final String description;

    Category(String description) {this.description = description;}


    public String getDescription() {
        return description;
    }

    public static class CategorySerializer extends JsonSerializer<Category> {

        @Override
        public void serialize(Category value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getDescription());
        }
    }
}
