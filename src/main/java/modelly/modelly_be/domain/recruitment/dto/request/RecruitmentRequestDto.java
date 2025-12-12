package modelly.modelly_be.domain.recruitment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import modelly.modelly_be.domain.recruitment.dto.common.RecruitmentSchedule;
import modelly.modelly_be.domain.recruitment.entity.enums.SubCategory;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

public record RecruitmentRequestDto(
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        @NotBlank(message = "제목은 필수입니다.")
        String title,
        @Schema(description = "공고 스케줄")
        @NotNull(message = "하나 이상은 필수입니다.")
        List<RecruitmentSchedule> recruitmentSchedule,
        @Schema(description = "카테고리", example = "HAIR, NAIL, TATTOO, MAKEUP, ETC 중 택1")
        @NotNull(message = "카테고리는 필수입니다.")
        Category category,
        @Schema(description = "세부 카테고리")
        List<SubCategory> subCategories,
        @Schema(description = "공고 내용", example = "시스루펌, 레이어드펌을 공짜로 받으실 헤어모델 모집합니다!")
        @NotBlank(message = "공고 내용은 필수입니다.")
        String content,
        @Schema(description = "전달사항", example = "머리 길이 어깨위로 올라오시는 분만 구합니다.")
        @NotBlank(message = "유의사항은 필수입니다")
        String notice,
        @Schema(description = "모델 목적1", example = "포트폴리오를 위해서")
        String goal1,
        @Schema(description = "모델 목적2", example = "포트폴리오를 위해서")
        String goal2,
        @Schema(description = "모델 목적3", example = "포트폴리오를 위해서")
        String goal3,
        @Schema(description = "공고관련 사진")
        List<String> imageUrls,
        @Schema(description = "영상촬영 동의", example = "false")
        @NotNull(message = "영상촬영 동의 여부는 필수입니다.")
        boolean agreeVideo,
        @Schema(description = "인스타 업로드 동의", example = "false")
        @NotNull(message = "인스타 동의 여부는 필수입니다")
        boolean agreeInsta,
        @Schema(description = "모자이크 가능 여부", example = "false")
        @NotNull(message = "모자이크 동의 여부는 필수입니다")
        boolean agreeMosaic,
        @Schema(description = "동의 사항 그 외", example = "신분증 필요")
        String etc
) {
}
