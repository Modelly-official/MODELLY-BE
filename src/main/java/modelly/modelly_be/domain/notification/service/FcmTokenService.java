package modelly.modelly_be.domain.notification.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.entity.FcmToken;
import modelly.modelly_be.domain.notification.repository.FcmTokenRepository;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.redis.RedisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final RedisService redisService;

    private static final String KEY_PREFIX = "fcm_token:";
    private static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 24 * 7;

    @Transactional(readOnly = true)
    public String getTokenByUserId(Long userId) {
        return fcmTokenRepository.findTokenByUserId(userId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_FCM_TOKEN));
    }

    @Transactional
    public void saveOrUpdateToken(Long userId, String token) {
        FcmToken fcmToken = fcmTokenRepository.findByUserId(userId)
                .map(existingToken -> {
                    existingToken.updateToken(token);
                    return existingToken;
                })
                .orElse(FcmToken.builder()
                        .userId(userId)
                        .token(token)
                        .build());

        fcmTokenRepository.save(fcmToken);
        String key = KEY_PREFIX + userId;
        redisService.setValue(key, token, TOKEN_EXPIRATION_TIME);
    }

    @Transactional
    public String getToken(Long userId) {
        String key = KEY_PREFIX + userId;

        String token = (String) redisService.getValue(key);
        if (token != null) {
            return token;
        }

        return fcmTokenRepository.findTokenByUserId(userId)
                .map(fcmToken -> {
                    redisService.setValue(key, fcmToken, TOKEN_EXPIRATION_TIME);
                    return fcmToken;
                })
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_FCM_TOKEN));
    }

    @Transactional
    public void deleteToken(Long userId) {
        fcmTokenRepository.findByUserId(userId)
                .ifPresent(fcmTokenRepository::delete);
        String key = KEY_PREFIX + userId;
        redisService.deleteValue(key);
    }
}
