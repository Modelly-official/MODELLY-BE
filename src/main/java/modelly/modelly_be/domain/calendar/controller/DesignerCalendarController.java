package modelly.modelly_be.domain.calendar.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.calendar.controller.swagger.DesignerCalendarSwagger;
import modelly.modelly_be.domain.calendar.dto.response.CalendarReservationDotsResponse;
import modelly.modelly_be.domain.calendar.dto.response.CalendarReservationResponse;
import modelly.modelly_be.domain.calendar.service.DesignerCalendarService;
import modelly.modelly_be.global.apiPayload.ApiResponse;
import modelly.modelly_be.global.security.AuthDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class DesignerCalendarController implements DesignerCalendarSwagger {

    private final DesignerCalendarService service;
    private final DesignerCalendarService designerCalendarService;

    @GetMapping("/designers/calendar/reservations")
    public ApiResponse<CalendarReservationResponse> getCalendarReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam String month, // yyyy-MM
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ApiResponse.onSuccess(
                service.getCalendarReservations(
                        auth.user(),
                        month,
                        date
                )
        );
    }

    @Override
    @GetMapping("/designers/calendar/reservation-dots")
    public ApiResponse<CalendarReservationDotsResponse> getReservationDots(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam String month,
            @RequestParam(defaultValue = "false") boolean includePending
    ) {
        return ApiResponse.onSuccess(
                designerCalendarService.getReservationDots(auth.user(), month, includePending)
        );
    }
}


