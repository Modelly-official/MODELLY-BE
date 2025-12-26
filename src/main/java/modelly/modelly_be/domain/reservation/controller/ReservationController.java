package modelly.modelly_be.domain.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.recruitment.service.RecruitmentService;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCreateRequest;
import modelly.modelly_be.domain.reservation.dto.response.AvailableReservationScheduleResponse;
import modelly.modelly_be.domain.reservation.service.ModelReservationService;
import modelly.modelly_be.domain.user.service.ModelService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ModelReservationService modelReservationService;
    private final RecruitmentService recruitmentService;
    private final ModelService modelService;

    /* ---------- 모델 관련 예약 API ---------- */
    // 예약하기
    @PostMapping("/models/reservations")
    public ApiResponse<SimpleMessageDTO> createReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestBody @Valid ReservationCreateRequest request
    ) {
        modelReservationService.createReservation(auth.user(), request);
        return ApiResponse.onSuccess(new SimpleMessageDTO("예약 신청이 완료되었습니다."));
    }

    // 특정 달의 예약 가능한 시간대 조회
    @GetMapping("/models/reservations/available-schedules")
    public ApiResponse<AvailableReservationScheduleResponse> getAvailableSchedules(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam Long recruitmentId,
            @RequestParam(required = false) String month
    ) {
        modelService.checkModel(auth.user());
        return ApiResponse.onSuccess(recruitmentService.getAvailableSchedules(recruitmentId, month));
    }
}
