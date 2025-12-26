package modelly.modelly_be.domain.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.reservation.dto.request.ReservationCreateRequest;
import modelly.modelly_be.domain.reservation.service.ModelReservationService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.apiPayload.code.SimpleMessageDTO;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ModelReservationService modelReservationService;

    @PostMapping
    public ApiResponse<SimpleMessageDTO> createReservation(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestBody @Valid ReservationCreateRequest request
    ) {
        modelReservationService.createReservation(auth.user(), request);
        return ApiResponse.onSuccess(new SimpleMessageDTO("예약 신청이 완료되었습니다."));
    }
}
