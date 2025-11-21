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
public class PhoneAuthService {

    private final StringRedisTemplate redisTemplate;
    private final SmsSender smsSender;

    private static final long AUTH_TTL_SECONDS = 3 * 60L;  // 인증번호 유효기간 - 5분
    private static final long RESEND_TTL_SECONDS = 60L;     // 재전송 제한 - 1분

    private static final String KEY_CODE_PREFIX = "PHONE_AUTH:CODE:";
    private static final String KEY_RESEND_PREFIX = "PHONE_AUTH:RESEND:";

    public void sendAuthCode(String phoneNumber) {
        try {
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            String resendKey = KEY_RESEND_PREFIX + phoneNumber;

            // 재요청 제한(제한 시간은 RESEND_TTL_SECONDS로 설정)
            Boolean exists = redisTemplate.hasKey(resendKey);
            if (Boolean.TRUE.equals(exists)) {
                throw new GeneralException(ErrorStatus.PHONE_AUTH_SEND_FREQUENT);
            }

            // 인증번호 생성
            String authCode = generateAuthCode();

            // Redis에 인증번호 저장(TTL은 AUTH_TTL_SECONDS)
            String codeKey = KEY_CODE_PREFIX + phoneNumber;
            ops.set(codeKey, authCode, AUTH_TTL_SECONDS, TimeUnit.SECONDS);

            // Redis에 재요청 제한 시간 저장(TTL은 RESEND_TTL_SECONDS)
            ops.set(resendKey, "BLOCK", RESEND_TTL_SECONDS, TimeUnit.SECONDS);

            // 문자 발송
            String message = "[Modelly] 본인확인 인증번호 [" + authCode + "]를 화면에 입력해주세요.";
            smsSender.send(phoneNumber, message);
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
                throw new GeneralException(ErrorStatus.PHONE_AUTH_EXPIRED);
            }

            if (!savedCode.equals(inputCode)) {
                throw new GeneralException(ErrorStatus.PHONE_AUTH_MISMATCH);
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
