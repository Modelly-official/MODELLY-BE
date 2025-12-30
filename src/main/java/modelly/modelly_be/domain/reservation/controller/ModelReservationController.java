package modelly.modelly_be.domain.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.reservation.controller.swagger.ModelReservationSwagger;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCreateRequest;
import modelly.modelly_be.domain.reservation.dto.response.AvailableReservationScheduleResponse;
import modelly.modelly_be.domain.reservation.dto.response.ModelReservationItem;
import modelly.modelly_be.domain.reservation.dto.response.ReservationScrollResponse;
import modelly.modelly_be.domain.reservation.entity.enums.ReservationListType;
import modelly.modelly_be.domain.reservation.service.ModelReservationService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.entity.Category;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ModelReservationController implements ModelReservationSwagger {

    private final ModelReservationService modelReservationService;

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
        return ApiResponse.onSuccess(modelReservationService.getAvailableSchedules(recruitmentId, month));
    }

    // 다가오는 일정, 완료된 일정 가져오기(무한스크롤)
    @GetMapping("/models/reservations")
    public ApiResponse<ReservationScrollResponse<ModelReservationItem>> getModelReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam(required = false) String month, // yyyy-MM
            @RequestParam ReservationListType type,
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime, // HH:mm
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(
                modelReservationService.getModelReservations(
                        auth.user(),
                        month,
                        type,
                        category,
                        size,
                        cursorDate,
                        cursorTime,
                        cursorId
                )
        );
    }

    // 리뷰 미작성 예약 조회
    @GetMapping("/models/reservations/unreviewed")
    public ApiResponse<ReservationScrollResponse<ModelReservationItem>> getUnreviewedCompletedReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam(required = false) String month, // yyyy-MM
            @RequestParam(required = false) Category category, // 전체면 안 보냄
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(
                modelReservationService.getModelCompletedUnreviewedReservations(
                        auth.user(),
                        month,
                        category,
                        size,
                        cursorDate,
                        cursorTime,
                        cursorId
                )
        );
    }
}
