package modelly.modelly_be.domain.recruitment.dto.response;


import java.util.List;

public record DesignerRecruitmentListResponse(
        Long recruitmentId,
        String title,
        String period,
        String thumbnail,
        Long reviewCount,
        Double averageRating,
        List<String> subCategory,

        boolean hasPendingReservation, // Pending 예약 존재 여부
        boolean hasConfirmedReservation, // 아직 끝나지 않은 확정된 예약 존재 여부
        boolean canModify
) {
}
