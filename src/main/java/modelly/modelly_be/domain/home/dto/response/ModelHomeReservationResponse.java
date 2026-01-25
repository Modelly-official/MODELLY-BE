package modelly.modelly_be.domain.home.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.user.entity.Designer;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.entity.SubCategory;
import modelly.modelly_be.global.formatter.TimeFormatter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record ModelHomeReservationResponse(
        Long reservationId,
        String designerNickname,
        String shop,
        String recruitmentTitle,
        @JsonSerialize(using = Category.CategorySerializer.class)
        Category category,
        List<String> subCategories,
        String startDateTime,
        long dDay
) {

    public static List<ModelHomeReservationResponse> from(List<Reservation> reservationList) {
        List<ModelHomeReservationResponse> responses = reservationList.stream()
                .map(reservation -> {
                    Designer designer = reservation.getDesigner();

                    // D-day 계산
                    long daysLeft = ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            reservation.getDate()
                    );

                    List<String> subCategories = reservation.getSubCategories().stream()
                            .map(SubCategory::getDescription)
                            .collect(Collectors.toList());

                    return ModelHomeReservationResponse.builder()
                            .reservationId(reservation.getId())
                            .designerNickname(designer.getNickname())
                            .shop(designer.getShop())
                            .recruitmentTitle(reservation.getRecruitment().getTitle())
                            .category(reservation.getCategory())
                            .subCategories(subCategories)
                            .startDateTime(TimeFormatter.parseStartTime(reservation.getDate(), reservation.getStartTime()))
                            .dDay(daysLeft)
                            .build();
                })
                .collect(Collectors.toList());

        return responses;
    }
}
