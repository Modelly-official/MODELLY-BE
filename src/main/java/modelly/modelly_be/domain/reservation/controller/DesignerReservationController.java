package modelly.modelly_be.domain.reservation.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.reservation.controller.swagger.DesignerReservationSwagger;
import modelly.modelly_be.domain.reservation.dto.response.*;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.domain.reservation.service.DesignerReservationService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class DesignerReservationController implements DesignerReservationSwagger {

    private final DesignerReservationService designerReservationService;

    /* ---------- 디자이너 관련 예약 API ---------- */
    @GetMapping("/designers/reservations")
    public ApiResponse<ReservationScrollResponse<DesignerReservationItem>> getDesignerReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam(required = false) String month, // yyyy-MM
            @RequestParam ReservationListType type,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime, // HH:mm
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(
                designerReservationService.getDesignerReservations(
                        auth.user(),
                        month,
                        type,
                        size,
                        cursorDate,
                        cursorTime,
                        cursorId
                )
        );
    }

    // 오늘의 예약 조회
    @GetMapping("/designers/reservations/daily")
    public ApiResponse<DesignerDailyReservationResponse> getDesignerDailyReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam LocalDate date
    ) {
        return ApiResponse.onSuccess(
                designerReservationService.getDailyReservations(auth.user(), date)
        );
    }

    // 신규 예약 조회 (무한스크롤)
    @GetMapping("/designers/reservations/pending")
    public ApiResponse<DesignerPendingReservationScrollResponse> getPendingReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(
                designerReservationService.getPendingReservations(
                        auth.user(),
                        size,
                        cursorDate,
                        cursorTime,
                        cursorId
                )
        );
    }

    @GetMapping("/designers/reservations/{reservationId}")
    public ApiResponse<DesignerReservationDetailResponse> getDesignerReservationDetail(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.onSuccess(
                designerReservationService.getReservationDetail(auth.user(), reservationId)
        );
    }

    // 예약 확정 (디자이너)
    @PostMapping("/designers/reservations/{reservationId}/confirm")
    public ApiResponse<SimpleMessageDTO> confirmReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId
    ) {
        SimpleMessageDTO res = designerReservationService.confirmPendingReservation(auth.user(), reservationId);
        return ApiResponse.onSuccess(res);
    }

    // 예약 거절 (디자이너)
    @PostMapping("/designers/reservations/{reservationId}/reject")
    public ApiResponse<SimpleMessageDTO> rejectReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId
    ) {
        SimpleMessageDTO res = designerReservationService.rejectPendingReservation(auth.user(), reservationId);
        return ApiResponse.onSuccess(res);
    }
}
