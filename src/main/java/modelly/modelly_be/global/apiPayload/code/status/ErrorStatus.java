package modelly.modelly_be.global.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.global.apiPayload.code.BaseErrorCode;
import modelly.modelly_be.global.apiPayload.code.ErrorReasonDTO;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 레디스 설정 오류
    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_ERROR", "Redis 설정에 오류가 발생했습니다."),

    // User
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER404", "해당 유저를 찾을 수 없습니다."),

    //Designer
    NOT_FOUND_DESIGNER(HttpStatus.NOT_FOUND, "DESIGNER404", "해당 디자이너를 찾을 수 없습니다."),

    //Model
    NOT_FOUND_MODEL(HttpStatus.NOT_FOUND, "MODEL404", "해당 모델을 찾을 수 없습니다."),

    // Token, JWT
    NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND,"TOKEN404","토큰을 찾을 수 없습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN401", "토큰이 유효하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN401", "Access Token이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN401", "Refresh Token이 만료되었습니다."),

    // Auth(로그인, 회원가입 관련)
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "AUTH409", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH409", "이미 사용 중인 이메일입니다."),
    SIGNUP_FIELDS_ERROR(HttpStatus.BAD_REQUEST, "AUTH400", "회원가입에 필요한 정보가 유효하지 않습니다."),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "AUTH401", "아이디 또는 비밀번호가 잘못 되었습니다."),
    DUPLICATE_USER_REGISTERED(HttpStatus.CONFLICT, "AUTH409", "이미 가입한 사용자입니다. 다른 방법으로 로그인을 시도해주세요."),
    SOCIAL_PROFILE_INCOMPLETE(HttpStatus.BAD_REQUEST, "USER405", "소셜 로그인 프로필 정보가 충분하지 않습니다."),

    // 휴대폰, 이메일 인증
    CODE_SEND_FREQUENT(HttpStatus.BAD_REQUEST, "CODE429", "인증번호는 1분 후에 다시 요청할 수 있습니다."),
    CODE_EXPIRED(HttpStatus.BAD_REQUEST, "CODE400", "인증번호가 만료되었습니다."),
    CODE_MISMATCH(HttpStatus.BAD_REQUEST, "CODE400", "인증번호가 일치하지 않습니다."),
    CODE_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "CODE500", "인증번호 발송에 실패했습니다."),

    //Recruitment
    NOT_FOUND_RECRUITMENT(HttpStatus.NOT_FOUND,"RECRUITMENT404", "공고를 찾을 수 없습니다."),
    FORBIDDEN_DELETE_OR_MODIFY_RECRUITMENT(HttpStatus.FORBIDDEN, "RECRUITMENT403", "작성자만 공고 삭제 및 수정이 가능합니다."),
    CAN_NOT_RECRUITMENT_DELETE_OR_MODIFY(HttpStatus.CONFLICT, "RECRUITMENT409", "현재 진행중이거나 확정된 예약이 있어 삭제 및 수정이 불가능합니다."),

    // 채팅
    INVALID_CHATROOM(HttpStatus.BAD_REQUEST, "CHAT400", "채팅방 생성이 불가능합니다."),
    NOT_FOUND_CHAT_ROOM(HttpStatus.NOT_FOUND, "CHAT404", "채팅방이 존재하지 않습니다."),

    //무한스크롤 관련 에러
    SCROLL_ERROR(HttpStatus.BAD_REQUEST, "SCROLL400", "무한스크롤 변환을 지원하지않는 엔티티입니다. ScrollUtil에 엔티티를 추가해주세요"),
    CURSOR_BAD_REQUEST(HttpStatus.BAD_REQUEST, "CURSOR400", "커서 정보가 유효하지않습니다. 형식에 맞춰서 다시 입력해주세요."),
    MONTH_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MONTH400","잘못된 month 형식입니다. 형식은 yyyy-MM 이어야 합니다"),

    GEOCODING_FAILED(HttpStatus.BAD_REQUEST, "GEOCODING400", "지오코딩에 실패하였습니다. 주소를 정확히 입력해주세요."),

    //s3
    FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "S3_ERROR", "파일 업로드에 실패했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
