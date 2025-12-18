package modelly.modelly_be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.infra.mail.EmailAuthType;
import modelly.modelly_be.domain.infra.mail.MailSender;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final StringRedisTemplate redisTemplate;
    private final MailSender mailSender;
    private final TemplateEngine templateEngine;

    private static final String PREFIX = "email-auth:";
    private static final Duration EXPIRE_TIME = Duration.ofMinutes(3);

    /* 인증번호 전송 */
    public void sendEmailCode(String email, EmailAuthType type) {
        String code = generateAuthCode();
        String key = redisKey(email, type);

        redisTemplate.opsForValue().set(key, code, EXPIRE_TIME);

        String subject = makeSubject(type);

        // 이메일 템플릿 멘트
        String mainTitle;      // 제일 굵은 제목
        String subText;        // 그 아래 설명
        String helpText;       // 맨 아래 안내문

        switch (type) {
            case FIND_ID -> {
                mainTitle = "아이디 찾기 인증 안내";
                subText = "아래 인증번호를 입력하시면\n회원님의 아이디를 확인하실 수 있습니다.";
                helpText = "인증번호를 정확히 입력해 주세요.";
            }
            case RESET_PASSWORD -> {
                mainTitle = "비밀번호 재설정 인증 안내";
                subText = "아래 인증번호를 입력하시면\n비밀번호를 재설정하실 수 있습니다.";
                helpText = "타인에게 인증번호를 공유하지 마세요.";
            }
            default -> throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        Context context = new Context();
        context.setVariable("code", code);
        context.setVariable("expireMinutes", EXPIRE_TIME.toMinutes());
        context.setVariable("mainTitle", mainTitle);
        context.setVariable("subText", subText);
        context.setVariable("helpText", helpText);
        context.setVariable("type", type.name());

        String html = templateEngine.process("email/auth-code", context);

        mailSender.sendHtml(email, subject, html);
    }

    /* 인증 번호 확인 */
    public void verifyEmailCode(String email, EmailAuthType type, String code) {
        String key = redisKey(email, type);
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new GeneralException(ErrorStatus.CODE_EXPIRED);
        }

        if (!savedCode.equals(code)) {
            throw new GeneralException(ErrorStatus.CODE_MISMATCH);
        }

        redisTemplate.delete(key);
    }

    /* Redis 키 prefix */
    private String redisKey(String email, EmailAuthType type) {
        return PREFIX + type.name() + ":" + email;
    }

    // 랜덤 6자리 인증번호 생성
    private String generateAuthCode() {
        int num = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", num); // 000000 ~ 999999
    }

    /* 메일 제목 설정 */
    private String makeSubject(EmailAuthType type) {
        return switch (type) {
            case FIND_ID -> "[Monde] 아이디 찾기 인증번호 안내";
            case RESET_PASSWORD -> "[Monde] 비밀번호 재설정 인증번호 안내";
        };
    }
}

