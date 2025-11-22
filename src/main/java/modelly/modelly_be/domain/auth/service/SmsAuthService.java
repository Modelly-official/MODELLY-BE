package modelly.modelly_be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.infra.sms.SmsSender;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SmsAuthService {

    private final StringRedisTemplate redisTemplate;
    private final SmsSender smsSender;

    private static final long AUTH_TTL_SECONDS = 3 * 60L;  // 인증번호 유효기간 - 3분
    private static final long RESEND_TTL_SECONDS = 60L;     // 재전송 제한 - 1분

    private static final String KEY_CODE_PREFIX = "PHONE_AUTH:CODE:";
    private static final String KEY_RESEND_PREFIX = "PHONE_AUTH:RESEND:";

    public void sendAuthCode(String phoneNumber) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String resendKey = KEY_RESEND_PREFIX + phoneNumber;
        String codeKey = KEY_CODE_PREFIX + phoneNumber;

        try {
            // 재요청 제한(제한 시간은 RESEND_TTL_SECONDS로 설정)
            Boolean success = ops.setIfAbsent(resendKey, "BLOCK", RESEND_TTL_SECONDS, TimeUnit.SECONDS);

            // 이미 키가 존재하면 false
            if (Boolean.FALSE.equals(success)) {
                throw new GeneralException(ErrorStatus.CODE_SEND_FREQUENT);
            }

            // 인증번호 생성
            String authCode = generateAuthCode();

            // Redis에 인증번호 저장(TTL은 AUTH_TTL_SECONDS)
            ops.set(codeKey, authCode, AUTH_TTL_SECONDS, TimeUnit.SECONDS);

            // 문자 발송
            String message = "[Modelly] 본인확인 인증번호 [" + authCode + "]를 화면에 입력해주세요.";
            smsSender.send(phoneNumber, message);
        } catch (GeneralException e) {
            // 문자 발송 실패 시 Redis 롤백
            if (e.getCode() == ErrorStatus.CODE_SEND_FAIL) {
                redisTemplate.delete(resendKey);
                redisTemplate.delete(codeKey);
            }
            throw e;
        } catch (DataAccessException e) {
            // Redis 관련 예외
            throw new GeneralException(ErrorStatus.REDIS_ERROR);
        }
    }

    public void verifyAuthCode(String phoneNumber, String inputCode) {
        try {
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            String codeKey = KEY_CODE_PREFIX + phoneNumber;
            String savedCode = ops.get(codeKey);

            if (savedCode == null) {
                throw new GeneralException(ErrorStatus.CODE_EXPIRED);
            }

            if (!savedCode.equals(inputCode)) {
                throw new GeneralException(ErrorStatus.CODE_MISMATCH);
            }

            // 성공 시 인증번호 삭제
            redisTemplate.delete(codeKey);
        } catch (DataAccessException e) {
            throw new GeneralException(ErrorStatus.REDIS_ERROR);
        }
    }

    // 랜덤 6자리 숫자 생성
    private String generateAuthCode() {
        int num = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", num); // 000000 ~ 999999
    }
}
