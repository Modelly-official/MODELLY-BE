package modelly.modelly_be.domain.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.domain.reservation.controller.swagger.ReservationSwagger;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCancelRequest;
import modelly.modelly_be.domain.reservation.dto.request.ReservationChangeCreateRequest;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCreateRequest;
import modelly.modelly_be.domain.reservation.dto.response.*;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.domain.reservation.service.DesignerReservationService;
import modelly.modelly_be.domain.reservation.service.ModelReservationService;
import modelly.modelly_be.domain.reservation.service.ReservationService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ReservationController implements ReservationSwagger {

    private final ReservationService reservationService;

    /* ---------- 모델/디자이너 공통 API ---------- */

    // 예약 변경 요청
    @PostMapping("/reservations/{reservationId}/changes")
    public ApiResponse<SimpleMessageDTO> createReservationChangeRequest(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId,
            @RequestParam(required = false) Long roomId,
            @Valid @RequestBody ReservationChangeCreateRequest request
    ) {
        reservationService.createChangeRequest(
                reservationId,
                roomId,
                auth.user(),
                request
        );

        return ApiResponse.onSuccess(new SimpleMessageDTO("변경 요청이 전송되었습니다."));
    }

    // 예약 변경 요청 수락
    @PostMapping("/reservations/changes/{reservationChangeId}/accept")
    public ApiResponse<SimpleMessageDTO> acceptReservationChange(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationChangeId
    ) {
        SimpleMessageDTO result = reservationService.acceptReservationChange(reservationChangeId, auth.user());
        return ApiResponse.onSuccess(result);
    }

    // 예약 변경 요청 취소
    @PostMapping("/reservations/changes/{reservationChangeId}/cancel")
    public ApiResponse<SimpleMessageDTO> cancelReservationChange(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationChangeId
    ) {
        SimpleMessageDTO res = reservationService.cancelReservationChange(reservationChangeId, auth.user());
        return ApiResponse.onSuccess(res);
    }

    // 예약 변경 요청 거절
    @PostMapping("/reservations/changes/{reservationChangeId}/reject")
    public ApiResponse<SimpleMessageDTO> rejectReservationChange(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationChangeId
    ) {
        SimpleMessageDTO result =
                reservationService.rejectReservationChange(reservationChangeId, auth.user());
        return ApiResponse.onSuccess(result);
    }

    // 예약 취소
    @PostMapping("/reservations/{reservationId}/cancel")
    public ApiResponse<SimpleMessageDTO> cancelReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long reservationChangeId,
            @RequestBody @Valid ReservationCancelRequest request
    ) {
        SimpleMessageDTO res = reservationService.cancelReservation(
                reservationId,
                roomId,
                reservationChangeId,
                auth.user(),
                request
        );
        return ApiResponse.onSuccess(res);
    }

    // 기존대로 진행
    @PostMapping("/reservations/changes/{reservationChangeId}/proceed")
    public ApiResponse<SimpleMessageDTO> proceedAsIs(
            @AuthenticationPrincipal AuthDetails auth,
            @PathVariable Long reservationChangeId
    ) {
        SimpleMessageDTO result = reservationService.proceedReservation(reservationChangeId, auth.user());
        return ApiResponse.onSuccess(result);
    }

}
