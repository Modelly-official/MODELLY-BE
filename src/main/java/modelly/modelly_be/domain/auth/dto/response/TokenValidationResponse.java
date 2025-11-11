package modelly.modelly_be.domain.auth.dto.response;

import lombok.*;
import modelly.modelly_be.global.security.entity.TokenStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenValidationResponse {
    private String message;
    private TokenStatus isValid;    // 유효 여부

    public static TokenValidationResponse of(String message, TokenStatus isValid) {
        TokenValidationResponse response = new TokenValidationResponse();
        response.message = message;
        response.isValid = isValid;
        return response;
    }

}
