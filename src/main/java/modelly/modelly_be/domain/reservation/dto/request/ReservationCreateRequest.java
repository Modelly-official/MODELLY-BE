package modelly.modelly_be.domain.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ReservationCreateRequest(
        @NotNull(message = "모집글 id는 필수입니다.")
        Long recruitmentId,

        @NotNull(message = "예약 날짜는 필수입니다.")
        LocalDate date,

        @NotBlank(message = "예약 시작 시간은 필수입니다.")
        String startTime,

        @NotNull(message = "카테고리는 필수입니다.")
        Category category,

        @NotEmpty(message = "세부 카테고리는 최소 1개 이상 선택해야 합니다.")
        List<SubCategory> subCategories,

        @NotBlank(message = "시술내역/현재상태 입력은 필수입니다.")
        String comment,

        @NotBlank(message = "디자이너 닉네임은 필수입니다.")
        String designerName,

        @NotBlank(message = "샵 이름은 필수입니다.")
        String shop,

        String imageUrls
) {}