package modelly.modelly_be.domain.notification.service.mapping;

import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static modelly.modelly_be.global.formatter.TimeFormatter.*;

@Service
public class ReservationNotificationService {

    //모델한테 예약 거절 알림 생성
    public NotificationData createReservationRejectNotification(Reservation reservation){

        String message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getDesignerName() +"디자이너";
        return NotificationData.otherNotification(
                reservation.getModel().getUser(),
                NotificationType.RESERVATION,
                "해당 예약은 확정되지 않았어요.",
                message,
                reservation.getId());

    }

    //모델한테 예약 확정 알림 생성
    @Transactional
    public NotificationData createReservationAcceptedNotification(Reservation reservation){

        String message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getDesignerName() +"디자이너";
        return NotificationData.otherNotification(
                reservation.getModel().getUser(),
                NotificationType.RESERVATION,
                "신청한 예약이 확정되었어요.",
                message,
                reservation.getId());

    }

    //디자이너한테 신규 예약 신청 알림 생성
    public NotificationData createReservationRequestNotification(Reservation reservation) {

        String message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getModel().getNickname() + "님";
        return NotificationData.otherNotification(
                reservation.getDesigner().getUser(),
                NotificationType.RESERVATION,
                "새로운 예약 신청이 있어요.",
                message,
                reservation.getId());

    }
}
