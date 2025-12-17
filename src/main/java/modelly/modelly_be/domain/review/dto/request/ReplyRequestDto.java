package modelly.modelly_be.domain.review.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReplyRequestDto(
        @NotNull(message = "답글 내용은 필수입니다.")
        @Size(max = 200, message = "내용은 최대 200자 이하입니다.")
       String content
) {
}
