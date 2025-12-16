package modelly.modelly_be.domain.review.dto.response;

import modelly.modelly_be.domain.review.entity.Reply;

import java.time.LocalDateTime;

public record ReplyResponseDto(
        Long replyId,
        String content,
        LocalDateTime createdAt
) {

    public static ReplyResponseDto of(Reply reply) {
        return new ReplyResponseDto(reply.getId(), reply.getContent(), reply.getCreatedAt());
    }
}
