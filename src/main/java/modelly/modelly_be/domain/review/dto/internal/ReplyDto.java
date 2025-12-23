package modelly.modelly_be.domain.review.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import modelly.modelly_be.domain.review.entity.Reply;

import java.time.LocalDateTime;

public record ReplyDto(
        @Schema(description = "답글 id", example = "1")
        Long replyId,
        @Schema(description = "디자이너 이름", example = "여노")
        String designerName,
        @Schema(description = "답글 내용", example = "멋진 리뷰 감사합니다 최고최고")
        String content,
        @Schema(description = "작성 일자")
        LocalDateTime createdAt
) {

    public static ReplyDto of(Reply reply) {
        return new ReplyDto(reply.getId(),
                reply.getDesigner().getNickname(),
                reply.getContent(),
                reply.getCreatedAt());
    }
}
