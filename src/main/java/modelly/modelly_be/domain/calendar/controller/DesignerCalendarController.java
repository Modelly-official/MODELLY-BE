package modelly.modelly_be.domain.calendar.controller;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.calendar.controller.swagger.DesignerCalendarSwagger;
import modelly.modelly_be.domain.calendar.dto.response.CalendarReservationScrollResponse;
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

    @GetMapping("/designers/calendar/reservations")
    public ApiResponse<CalendarReservationScrollResponse> getCalendarReservations(
            @AuthenticationPrincipal AuthDetails auth,
            @RequestParam String month, // yyyy-MM
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cursorDate,
            @RequestParam(required = false) String cursorTime,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.onSuccess(
                service.getCalendarReservations(
                        auth.user(),
                        month,
                        date,
                        size,
                        cursorDate,
                        cursorTime,
                        cursorId
                )
        );
    }
}


