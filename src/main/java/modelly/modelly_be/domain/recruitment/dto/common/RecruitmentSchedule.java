package modelly.modelly_be.domain.recruitment.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record RecruitmentSchedule(
        @Schema(description = "공고 일자", example = "2025-11-28")
        @NotNull(message = "하루 이상은 필수입니다.")
        LocalDate recruitmentDate,
        @Schema(description = "공고 시간대", example = "[ \"10:30\"],")
        @NotNull(message = "하나 이상은 필수입니다.")
        List<String> recruitmentTimes
) {

    public static RecruitmentSchedule of(LocalDate recruitmentDate, List<String> recruitmentTime) {
        return new RecruitmentSchedule(recruitmentDate, recruitmentTime);
    }
}
