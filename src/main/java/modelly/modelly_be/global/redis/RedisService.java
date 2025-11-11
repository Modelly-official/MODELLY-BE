package modelly.modelly_be.global.redis;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisService {
    // Redis 연결
    private final RedisTemplate<String, Object> redisTemplate;

//    @Value("${jwt.token.refresh-expiration-time}")
//    private long refreshTokenExpirationTime;
//
//    @Value("${jwt.token.access-expiration-time}")
//    private long accessExpirationTime;

    // RefreshToken TTL 설정
    public void setRefreshToken(String key, String value, long ttlSeconds) {
        try {
            ValueOperations<String, Object> values = redisTemplate.opsForValue();
            values.set(key, value, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.REDIS_ERROR);
        }
    }

    // 값 조회(없으면 빈 문자열)
    public String getValue(String key) {
        try {
            ValueOperations<String, Object> values = redisTemplate.opsForValue();
            Object value = values.get(key);
            return value == null ? "" : value.toString();
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.REDIS_ERROR);
        }
    }

    // 키 삭제
    public void deleteValue(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.REDIS_ERROR);
        }
    }

    // 해당 키가 Redis에 존재하는지 확인
    public boolean checkExistsValue(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.REDIS_ERROR);
        }
    }

}
