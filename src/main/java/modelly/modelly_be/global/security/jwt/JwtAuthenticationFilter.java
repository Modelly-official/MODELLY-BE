package modelly.modelly_be.global.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = tokenProvider.resolveToken(request);

        if (token != null) {
            try{
                // Authentication 생성
                Authentication authentication = tokenProvider.getAuthentication(token);

                if (authentication != null) {
                    // SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                handleJwtException(response, ErrorStatus.ACCESS_TOKEN_EXPIRED);
                return;
            } catch (SecurityException | MalformedJwtException |
                     UnsupportedJwtException | IllegalArgumentException e) {
                handleJwtException(response, ErrorStatus.TOKEN_INVALID);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleJwtException(HttpServletResponse response, ErrorStatus status) throws IOException {
        response.setStatus(status.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        String body = String.format(
                "{\"isSuccess\":false,\"code\":\"%s\",\"message\":\"%s\"}",
                status.getCode(),
                status.getMessage()
        );
        response.getWriter().write(body);
    }

}
