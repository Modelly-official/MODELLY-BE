package modelly.modelly_be.global.security.jwt;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import modelly.modelly_be.global.security.entity.TokenStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        // CONNECT 시 토큰 검사
        if (StompCommand.CONNECT.equals(command)) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // 채팅은 로그인 필수라고 가정
                throw new GeneralException(ErrorStatus.TOKEN_INVALID);
            }

            String token = authHeader.substring(7);

            TokenStatus status = tokenProvider.validateToken(token);
            if (status != TokenStatus.VALID) {
                // 만료/유효하지 않음 모두 예외 처리 (필요하면 상태별로 다르게)
                throw new GeneralException(ErrorStatus.TOKEN_INVALID);
            }

            // 기존 HTTP 필터에서 쓰던 로직 재사용
            Authentication authentication = tokenProvider.getAuthentication(token);

            // WebSocket 세션의 Principal로 심기
            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        return message;
    }
}
