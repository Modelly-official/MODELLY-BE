package modelly.modelly_be.domain.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

public record RecruitmentRequestDto(
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
                @NotNull(message = "제목은 필수입니다.")
        String title,
        @Schema(description = "공고 스케줄")
        @NotNull(message = "제목은 필수입니다.")
        List<RecruitmentSchedule> recruitmentSchedule,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        Category category,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        String content,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        String notice,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        String goal1,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        String goal2,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        String goal3,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        List<String> imageUrls,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        boolean agreeVideo,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        boolean agreeInsta,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        boolean agreeMosaic,
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotNull(message = "제목은 필수입니다.")
        String etc
) {
}
