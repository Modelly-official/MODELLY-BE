package modelly.modelly_be.global.security.jwt;

import org.springframework.http.ResponseCookie;

public final class CookieUtil {

    public static final String REFRESH_COOKIE = "refresh_token";

    private CookieUtil() {}

    /* 리프레시 토큰 쿠키 생성 */
    public static ResponseCookie buildRefreshCookie(
            String value,
            long maxAgeSec,
            String domain,
            boolean secure,
            String sameSite
    ) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAgeSec);

        if (domain != null && !domain.isBlank()) builder.domain(domain);
        if (sameSite != null && !sameSite.isBlank()) builder.sameSite(sameSite);
        builder.secure(secure);

        return builder.build();
    }

    /* 리프레시 토큰 쿠키 제거 */
    public static ResponseCookie deleteRefreshCookie(String domain, boolean secure, String sameSite) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0);

        if (domain != null && !domain.isBlank()) builder.domain(domain);
        if (sameSite != null && !sameSite.isBlank()) builder.sameSite(sameSite);
        builder.secure(secure);

        return builder.build();
    }
}
