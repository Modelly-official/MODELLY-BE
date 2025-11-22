package modelly.modelly_be.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsAuthRequest {

    /* 전화번호 인증 요청 DTO*/
    @NotBlank
    private String phoneNumber; // 01012345678 형식(-,* 같은 특수문자 X)

}
