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
        @NotNull Long recruitmentId,
        @NotNull LocalDate date,
        @NotNull String startTime,
        @NotNull Category category,
        @NotEmpty List<SubCategory> subCategories,
        @NotBlank String comment,
        @NotBlank String designerName,
        @NotBlank String shop,
        @NotBlank String imageUrls
) {}