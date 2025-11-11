package modelly.modelly_be.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.global.security.dto.TokenResponse;
import modelly.modelly_be.global.security.entity.TokenStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.security.Key;
import java.util.Date;


@Component
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;

    @Value("${jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    private final UserDetailsService userDetailsService;

    // key 초기화
    @Override
    public void afterPropertiesSet(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성
    public String createAccessToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessExpirationTime);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId())) // 사용자 ID
                .setIssuedAt(now) // 발급 시각
                .setExpiration(expiration) // 만료 시각
                .signWith(key, SignatureAlgorithm.HS512) // 암호화 알고리즘
                .compact();
    }

    // RefreshToken 생성
    public String createRefreshToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
    }

    // access, refresh token 반환
    public TokenResponse createToken(User user) {
        return TokenResponse.of(createAccessToken(user), createRefreshToken(user));
    }

    // 토큰 만료 시간 조회
    public Long getExpirationTime(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .getTime();
    }

    // 요청 헤더에서 토큰 꺼내기
    public String resolveToken(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    //userId 추출
    public String getUserIdFromToken(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 토큰 -> 인증 객체 변환
    public Authentication getAuthentication(String token){
        UserDetails userDetails =
                (UserDetails) userDetailsService.loadUserByUsername(getUserIdFromToken(token));
        return new UsernamePasswordAuthenticationToken(
                userDetails, token, userDetails.getAuthorities());
    }

    // 토큰 유효성 검사(TokenStatus를 도입한게 의미가 있나..? -> 리팩토링 고려)
    public TokenStatus validateToken(String token) {
        if (token == null) return TokenStatus.MISSING;
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return TokenStatus.VALID;
        } catch (ExpiredJwtException ex) {
            return TokenStatus.EXPIRED;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            return TokenStatus.INVALID;
        }
    }

    public long getRemainingMillis(String token) {
        return getExpirationTime(token) - System.currentTimeMillis();
    }

    public long getRemainingSeconds(String token) {
        return Math.max(1, getRemainingMillis(token) / 1000);
    }

}
