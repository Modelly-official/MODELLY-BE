package modelly.modelly_be.domain.auth.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DuplicateCheckResponse {
    private String field;   // loginId or email
    private String value;
    private boolean available; // true(중복 X)

    public static DuplicateCheckResponse of(String field, String value, boolean available) {
        DuplicateCheckResponse response = new DuplicateCheckResponse();
        response.field = field;
        response.value = value;
        response.available = available;
        return response;
    }
}
