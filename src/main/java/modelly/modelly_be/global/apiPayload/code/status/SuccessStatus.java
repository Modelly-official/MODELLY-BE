package modelly.modelly_be.global.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.global.apiPayload.code.BaseCode;
import modelly.modelly_be.global.apiPayload.code.ReasonDTO;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    // 가장 일반적인 응답
    OK(HttpStatus.OK, "200", "요청이 성공했습니다."),
    CREATED(HttpStatus.CREATED, "201", "생성이 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}
