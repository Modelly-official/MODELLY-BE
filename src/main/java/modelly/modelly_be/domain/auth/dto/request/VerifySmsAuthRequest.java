package modelly.modelly_be.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class VerifySmsAuthRequest {
    /* 인증번호 검증 요청 DTO*/
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String authCode; // 6자리 숫자

}
