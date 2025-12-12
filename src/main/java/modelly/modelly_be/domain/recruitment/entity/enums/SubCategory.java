package modelly.modelly_be.domain.recruitment.entity.enums;

public enum SubCategory {
    //HAIR
    HAIR_CUT("커트"),
    HAIR_PERM("파마"),
    HAIR_COLORING("염색"),
    //NAIL
    ONE_COLOR("원컬러"),
    ART("네일아트"),
    PEDICURE("페디큐어"),
    //EYELASH
    EYELASH_PERM("펌"),
    EYELASH_EXTENSION("연장"),
    //TATTOO
    LIP_TATTOO("립"),
    EYEBROW_TATTOO("눈썹"),
    NORMAL_TATTOO("일반아트"),

    ETC("기타")
    ;

    private final String description;

    SubCategory(String description) {this.description = description;}

    public String getDescription() {
        return description;
    }
}
