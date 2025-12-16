package modelly.modelly_be.domain.review.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReplyRequestDto(
        @NotNull(message = "답글 내용은 필수입니다.")
        @Size(max = 1000, message = "내용은 최대 1000자 이하입니다.")
       String content
) {
}
