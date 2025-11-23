package modelly.modelly_be.domain.auth.service;

import modelly.modelly_be.domain.auth.dto.request.*;
import modelly.modelly_be.domain.auth.dto.response.FindIdResponse;
import modelly.modelly_be.domain.infra.mail.EmailAuthType;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AccountRecoveryService {
    /* 아이디 찾기/비밀번호 재설정를 위한 Service */

    private final UserRepository userRepository;
    private final EmailAuthService emailAuthService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    // 이메일 인증 완료 플래그(공통)
    private static final String EMAIL_VERIFIED_PREFIX = "email-verified:";  // email-verified:{type}:{email}
    private static final Duration EMAIL_VERIFY_TTL = Duration.ofMinutes(3); // 유효기간 10분

    // 비밀번호 재설정용 userId 저장
    private static final String PW_RESET_VERIFIED_PREFIX = "pw-reset-verified:"; // pw-reset-verified:{email}
    private static final Duration PW_RESET_VERIFY_TTL = Duration.ofMinutes(10); // 유효기간 10분

    // 인증번호 검증 + 인증 완료 플래그 저장
    public void verifyEmailCode(EmailCodeVerifyRequest request) {
        // 인증번호 검증
        emailAuthService.verifyEmailCode(request.getEmail(), request.getType(), request.getAuthCode());

        // 인증 완료 플래그 저장
        String key = buildVerifiedKey(request.getType(), request.getEmail());
        redisTemplate.opsForValue().set(key, "true", EMAIL_VERIFY_TTL);
    }

    /* 이메일 인증 여부 Prefix(email-verified:{type}:{email}) */
    private String buildVerifiedKey(EmailAuthType type, String email) {
        return EMAIL_VERIFIED_PREFIX + type.name() + ":" + email;
    }

    private void checkVerifiedEmail(EmailAuthType type, String email) {
        String key = buildVerifiedKey(type, email);
        Boolean exists = redisTemplate.hasKey(key);
        if (!Boolean.TRUE.equals(exists)) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        redisTemplate.delete(key);
    }


    /* ---------- 아이디 찾기 ----------- */

    // 1. 이름, 이메일 일치하는지 확인 후 Email 전송
    public void sendFindIdCode(FindIdRequest request) {
        userRepository.findByNameAndEmail(request.getName(), request.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        emailAuthService.sendEmailCode(request.getEmail(), EmailAuthType.FIND_ID);
    }
    // 2. 이메일 인증(verifyEmailCode)
    // 3. 아이디 반환
    public FindIdResponse findId(FindIdRequest request) {
        // Redis에 이메일 인증 완료 여부 확인 후 삭제
        checkVerifiedEmail(EmailAuthType.FIND_ID, request.getEmail());

        User user = userRepository.findByNameAndEmail(request.getName(), request.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        return new FindIdResponse(user.getLoginType(), user.getName(), user.getLoginId(), user.getEmail());
    }


    /* ---------- 비밀번호 찾기 ---------- */

    // 1. 이름, 로그인 ID, 이메일 일치하는지 확인 후 인증 코드 발송
    public void sendResetPasswordCode(ResetPasswordRequest request) {
        userRepository.findByNameAndLoginIdAndEmail(request.getName(), request.getLoginId(), request.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        emailAuthService.sendEmailCode(request.getEmail(), EmailAuthType.RESET_PASSWORD);
    }
    // 2. 이메일 인증(verifyEmailCode)
    // 3. 이름/아이디/이메일 검증 + 이메일 인증 여부 확인 + userId를 Redis에 저장
    @Transactional
    public void verifyResetPassword(ResetPasswordRequest request) {
        // 이메일 인증 완료 여부 확인
        checkVerifiedEmail(EmailAuthType.RESET_PASSWORD, request.getEmail());

        // 이름 + 아이디 + 이메일로 실제 유저 확인
        User user = userRepository.findByNameAndLoginIdAndEmail(request.getName(), request.getLoginId(), request.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        // 비밀번호 재설정 전용 플래그: 이 이메일은 해당 userId로 재설정 허용
        String key = PW_RESET_VERIFIED_PREFIX + request.getEmail();
        redisTemplate.opsForValue().set(key, user.getId().toString(), PW_RESET_VERIFY_TTL);
    }

    // 4. 비밀번호 변경
    @Transactional
    public void resetPassword(ResetPasswordConfirmRequest request) {

        // 이메일 인증한 유저 정보 가져오기
        String key = PW_RESET_VERIFIED_PREFIX + request.getEmail();
        String userIdStr = redisTemplate.opsForValue().get(key);

        // 인증 만료 혹은 인증 안한 유저일 경우
        if (userIdStr == null) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }

        Long userId = Long.valueOf(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND_USER));

        // 비밀번호 변경
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));

        redisTemplate.delete(key);

    }
}

