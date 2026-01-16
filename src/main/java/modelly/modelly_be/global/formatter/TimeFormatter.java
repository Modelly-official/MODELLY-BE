package modelly.modelly_be.global.formatter;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Component
public class TimeFormatter {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String parseStartTime(LocalDate date, LocalTime startTime) {
        try {
            LocalDateTime dateTime = LocalDateTime.of(date, startTime);
            return dateTime.format(DateTimeFormatter.ofPattern("M월 d일 HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("잘못된 time 형식: " + startTime, e);
        }
    }

    public static String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        Duration duration = Duration.between(dateTime, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1){
            return "방금 전";
        } else if (hours < 1) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (days == 1) {
            return "어제";
        } else if (days < 7) {
            return days + "일 전";
        } else if (days < 365){
            return dateTime.format(DateTimeFormatter.ofPattern("M월 d일"));
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }
    }
}
