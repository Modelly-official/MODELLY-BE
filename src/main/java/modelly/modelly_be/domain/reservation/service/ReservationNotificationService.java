package modelly.modelly_be.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.notification.service.NotificationService;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.entity.enums.UserRole;
import modelly.modelly_be.global.formatter.TimeFormatter;
import org.springframework.stereotype.Service;

import static modelly.modelly_be.global.formatter.TimeFormatter.*;

@Service
@RequiredArgsConstructor
public class ReservationNotificationService {

    private final NotificationService notificationService;

    //모델한테 예약 거절 알림 전송
    public void createReservationRejectNotification(Reservation reservation){

        String message = parseStartTime(reservation.getStartTime());
        NotificationData notificationData = NotificationData.otherNotification(
                reservation.getModel().getUser(),
                NotificationType.RESERVATION,
                "해당 예약은 확정되지 않았어요.",
                message,
                reservation.getId());

        notificationService.createNotification(notificationData);
    }

    //모델한테 예약 확정 알림 전송
    public void createReservationAcceptedNotification(Reservation reservation){

        String message = parseStartTime(reservation.getStartTime()) + reservation+reservation.getDesignerName() +"디자이너";
        NotificationData notificationData = NotificationData.otherNotification(
                reservation.getModel().getUser(),
                NotificationType.RESERVATION,
                "신청한 예약이 확정되었어요.",
                message,
                reservation.getId());

        notificationService.createNotification(notificationData);
    }

    //일정 취소 알림
    public void createScheduleCancelNotification(User user, Reservation reservation, Long finalRoomId) {

        String title ="";
        String message ="";
        String senderName ="";

        switch (user.getUserRole()) {
            case MODEL -> {
                title = "디자이너 요청으로 취소된 예약 일정이 있어요.";
                message = parseStartTime(reservation.getStartTime()) + reservation+reservation.getDesignerName() +"디자이너";
                senderName = reservation.getDesignerName();
            }
            case DESIGNER -> {
                title = "모델의 요청으로 취소된 예약 일정이 있어요.";
                message = parseStartTime(reservation.getStartTime()) + reservation.getModel().getNickname() + "님";
                senderName = reservation.getModel().getNickname();
            }
        }

        NotificationData notificationData = NotificationData.chattingNotification(user, NotificationType.SCHEDULE, title, message, finalRoomId, senderName);
        notificationService.createNotification(notificationData);
    }

    //디자이너한테 신규 예약 신청 알림 전송
    public void createReservationRequestNotification(Reservation reservation) {

        String message = parseStartTime(reservation.getStartTime()) + reservation.getModel().getNickname() + "님";
        NotificationData notificationData = NotificationData.otherNotification(
                reservation.getDesigner().getUser(),
                NotificationType.RESERVATION,
                "새로운 예약 신청이 있어요.",
                message,
                reservation.getId());

        notificationService.createNotification(notificationData);
    }
}
