package modelly.modelly_be.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (GeneralException e) {
            setErrorResponse(response,e.getErrorReasonHttpStatus().getHttpStatus().value(),e.getErrorReasonHttpStatus().getCode(),e.getErrorReasonHttpStatus().getMessage());
        }
    }

    private void setErrorResponse(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        String json = objectMapper.writeValueAsString(Map.of(
                "isSuccess", false,
                "code", code,
                "message", message
        ));

        response.getWriter().write(json);
    }
}
