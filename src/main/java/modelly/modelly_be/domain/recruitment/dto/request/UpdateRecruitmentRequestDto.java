package modelly.modelly_be.domain.recruitment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import modelly.modelly_be.domain.recruitment.dto.internal.RecruitmentSchedule;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.global.entity.Category;

import java.util.List;

public record UpdateRecruitmentRequestDto(
        @Schema(description = "공고 제목", example = "헤어모델 모집합니다.")
        String title,
        @Schema(description = "공고 스케줄")
        List<RecruitmentSchedule> recruitmentSchedule,
        @Schema(description = "카테고리", example = "HAIR, NAIL, TATTOO, EYELASH 중 택1")
        Category category,
        @Schema(description = "세부 카테고리")
        List<SubCategory> subCategoryList,
        @Schema(description = "공고 내용", example = "시스루펌, 레이어드펌을 공짜로 받으실 헤어모델 모집합니다!")
        String content,
        @Schema(description = "전달사항", example = "머리 길이 어깨위로 올라오시는 분만 구합니다.")
        String notice,
        @Schema(description = "모델 목적1", example = "포트폴리오를 위해서")
        String goal1,
        @Schema(description = "공고 썸네일")
        String thumbnail,
        @Schema(description = "공고관련 사진")
        List<String> imageUrls,
        @Schema(description = "공고 사진이 저장된 폴더 id. presignedUrl 발급 후, 응답 dto의 folderId을 넣어주세요.")
        String imageFolderId,
        @Schema(description = "영상촬영 동의", example = "false")
        boolean agreeVideo,
        @Schema(description = "인스타 업로드 동의", example = "false")
        boolean agreeInsta,
        @Schema(description = "모자이크 가능 여부", example = "false")
        boolean agreeMosaic,
        @Schema(description = "동의 사항 그 외", example = "신분증 필요")
        String etc
) {
}
