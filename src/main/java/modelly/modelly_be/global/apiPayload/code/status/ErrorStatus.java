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
    SUBCATEGORY_MISMATCH(HttpStatus.BAD_REQUEST, "RECRUITMENT400", "선택한 서브 카테고리가 상위 카테고리와 일치하지 않습니다."),
    NOT_FOUND_RECRUITMENT_TIME(HttpStatus.NOT_FOUND,"RECRUITMENT404", "공고에서 선택한 일시를 찾을 수 없습니다."),

    // 채팅
    INVALID_CHATROOM(HttpStatus.BAD_REQUEST, "CHAT400", "채팅방 생성이 불가능합니다."),
    NOT_FOUND_CHAT_ROOM(HttpStatus.NOT_FOUND, "CHAT404", "채팅방이 존재하지 않습니다."),
    TOO_MANY_FILES(HttpStatus.BAD_REQUEST, "CHAT400", "첨부한 이미지 파일이 너무 많습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "CHAT400", "첨부한 파일의 크기가 너무 큽니다."),

    //무한스크롤 관련 에러
    SCROLL_ERROR(HttpStatus.BAD_REQUEST, "SCROLL400", "무한스크롤 변환을 지원하지않는 엔티티입니다. ScrollUtil에 엔티티를 추가해주세요"),
    CURSOR_BAD_REQUEST(HttpStatus.BAD_REQUEST, "CURSOR400", "커서 정보가 유효하지않습니다. 형식에 맞춰서 다시 입력해주세요."),
    MONTH_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MONTH400","잘못된 month 형식입니다. 형식은 yyyy-MM 이어야 합니다"),

    //GEOCODING
    GEOCODING_FAILED(HttpStatus.BAD_REQUEST, "GEOCODING400", "지오코딩에 실패하였습니다. 주소를 정확히 입력해주세요."),
    COORDINATE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COORDINATE400", "거리 정렬 시 사용자 좌표가 필요합니다."),

    //Reservation
    NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "RESERVATION404", "예약이 존재하지 않습니다."),
    RESERVATION_TIME_CONFLICT(HttpStatus.CONFLICT, "RESERVATION409", "이미 예약된 시간대입니다. 다른 시간을 선택해주세요."),
    RESERVATION_BAD_REQUEST(HttpStatus.BAD_REQUEST, "RESERVATION400", "잘못된 예약 상태 변경 요청입니다."),
    RESERVATION_CHANGE_ALREADY_PENDING(HttpStatus.CONFLICT, "RESERVATION409", "이미 변경 요청한 예약입니다."),
    NOT_FOUND_RESERVATION_CHANGE(HttpStatus.NOT_FOUND, "RESERVATION404", "예약 변경 요청이 존재하지 않습니다."),
    RESERVATION_CHANGE_NOT_PENDING(HttpStatus.FORBIDDEN, "RESERVATION403", "예약 변경 대기 중인 요청이 아닙니다."),
    RESERVATION_CHANGE_SELF_RESPONSE_NOT_ALLOWED(HttpStatus.FORBIDDEN,"RESERVATION403", "예약 변경 요청한 본인이 수락/거절할 수 없습니다."),
    RESERVATION_CANCEL_TOO_LATE(HttpStatus.BAD_REQUEST, "RESERVATION400", "예약 시작 시각 기준으로 72시간 이전까지만 취소 가능합니다."),
    RESERVATION_CHANGE_NOT_REJECTED(HttpStatus.FORBIDDEN, "RESERVATION403", "거절된 예약 변경 요청이 아닙니다."),
    RESERVATION_CHANGE_ONLY_REQUESTER_CAN_PROCEED(HttpStatus.FORBIDDEN, "RESERVATION403", "예약 변경 요청자만 기존대로 진행을 선택할 수 있습니다."),

    //Review
    FORBIDDEN_CREATE_REVIEW(HttpStatus.FORBIDDEN, "REVIEW403", "예약자만 리뷰 작성이 가능합니다."),
    REVIEW_ALREADY_EXIST(HttpStatus.CONFLICT, "REVIEW409", "해당 예약에 대한 리뷰를 이미 작성하셨습니다."),
    NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, "REVIEW404", "리뷰가 존재하지 않습니다."),
    RESERVATION_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "REVIEW400", "예약된 일정이 아직 완료되지않아 리뷰 작성이 불가능합니다."),
    FORBIDDEN_DELETE_OR_MODIFY_REVIEW(HttpStatus.FORBIDDEN, "REVIEW403", "작성자만 리뷰 삭제 및 수정이 가능합니다."),
    FORBIDDEN_UPDATE_FIX(HttpStatus.FORBIDDEN, "REVIEW403", "해당 리뷰의 디자이너만 리뷰 고정 / 고정 취소가 가능합니다."),
    NOT_FOUND_REPLY(HttpStatus.NOT_FOUND, "REPLY404", "답글이 존재하지 않습니다."),
    FORBIDDEN_MODIFY_REPLY(HttpStatus.FORBIDDEN, "REPLY403", "해당 답글의 디자이너만 답글 수정이 가능합니다."),
    REPLY_ALREADY_EXIST(HttpStatus.CONFLICT, "REPLY409", "해당 리뷰에 대한 답글을 이미 작성하셨습니다."),

    //s3
    FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "S3_ERROR", "파일 업로드에 실패했습니다."),

    //Portfolio
    NOT_FOUND_PORTFOLIO(HttpStatus.NOT_FOUND, "PORTFOLIO404", "포트폴리오가 존재하지 않습니다."),
    FORBIDDEN_MODIFY_OR_DELETE_PORTFOLIO(HttpStatus.FORBIDDEN,"PORTFOLIO403", "작성자만 포트폴리오 수정 및 삭제가 가능합니다."),

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
