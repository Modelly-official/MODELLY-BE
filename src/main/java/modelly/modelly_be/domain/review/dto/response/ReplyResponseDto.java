package modelly.modelly_be.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import modelly.modelly_be.domain.review.entity.Reply;

import java.time.LocalDateTime;

public record ReplyResponseDto(
        @Schema(description = "답글 id", example="1")
        Long replyId,
        @Schema(description = "답글 내용", example="멋진 리뷰 감사합니다. 최고최고")
        String content,
        @Schema(description = "작성 일시")
        LocalDateTime createdAt
) {

    public static ReplyResponseDto of(Reply reply) {
        return new ReplyResponseDto(reply.getId(), reply.getContent(), reply.getCreatedAt());
    }
}
