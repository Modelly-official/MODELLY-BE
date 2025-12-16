package modelly.modelly_be.domain.review.dto.common;

import modelly.modelly_be.domain.review.entity.Reply;

import java.time.LocalDateTime;

public record ReplyDto(
        Long replyId,
        String designerName,
        String content,
        LocalDateTime createdAt
) {

    public static ReplyDto of(Reply reply) {
        return new ReplyDto(reply.getId(),
                reply.getDesigner().getNickname(),
                reply.getContent(),
                reply.getCreatedAt());
    }
}
