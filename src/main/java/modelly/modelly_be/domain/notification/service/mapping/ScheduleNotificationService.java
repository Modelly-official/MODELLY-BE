package modelly.modelly_be.domain.notification.service.mapping;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import modelly.modelly_be.domain.notification.entity.NotificationType;
import modelly.modelly_be.domain.reservation.entity.Reservation;
import modelly.modelly_be.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static modelly.modelly_be.global.formatter.TimeFormatter.parseStartTime;

@Service
@RequiredArgsConstructor
public class ScheduleNotificationService {

    //일정 변동 알림
    public NotificationData createScheduleChangeNotification(User user, Reservation reservation, Long finalRoomId) {
        String title = "예약 변경 요청이 있어요. 상세 내용 확인 후 수락 혹은 거절을 진행해주세요.";
        String message = "";
        String senderName = "";

        switch (user.getUserRole()){
            case MODEL -> {
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getDesignerName() +" 디자이너";
                senderName = reservation.getDesignerName();
            }
            case DESIGNER -> {
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getModel().getNickname() + "님";
                senderName = reservation.getModel().getNickname();
            }
        }

        String notificationTypeDescription = NotificationType.SCHEDULE.toDisplayScheduleType(title);

        return NotificationData.chattingNotification(user.getId(), NotificationType.SCHEDULE, notificationTypeDescription, title, message, finalRoomId, senderName);}

    //일정 취소 알림
    public NotificationData createScheduleCancelNotification(User user, Reservation reservation, Long finalRoomId) {

        String title ="";
        String message ="";
        String senderName ="";

        switch (user.getUserRole()) {
            case MODEL -> {
                title = "디자이너 요청으로 취소된 예약 일정이 있어요.";
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getDesignerName() +" 디자이너";
                senderName = reservation.getDesignerName();
            }
            case DESIGNER -> {
                title = "모델의 요청으로 취소된 예약 일정이 있어요.";
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getModel().getNickname() + "님";
                senderName = reservation.getModel().getNickname();
            }
        }

        String notificationTypeDescription = NotificationType.SCHEDULE.toDisplayScheduleType(title);

        return NotificationData.chattingNotification(user.getId(), NotificationType.SCHEDULE, notificationTypeDescription, title, message, finalRoomId, senderName);
    }

    @Transactional
    public NotificationData createScheduleChangeRejectNotification(User user, Reservation reservation, Long finalRoomId) {
        String title ="예약 변경 요청이 거절되었습니다.";
        String message ="";
        String senderName ="";

        switch (user.getUserRole()) {
            case MODEL -> {
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getDesignerName() +" 디자이너";
                senderName = reservation.getDesignerName();
            }
            case DESIGNER -> {
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getModel().getNickname() + "님";
                senderName = reservation.getModel().getNickname();
            }
        }

        String notificationTypeDescription = NotificationType.SCHEDULE.toDisplayScheduleType(title);

        return NotificationData.chattingNotification(user.getId(), NotificationType.SCHEDULE, notificationTypeDescription, title, message, finalRoomId, senderName);

    }

    @Transactional
    public NotificationData createScheduleChangeAcceptNotification(User user, Reservation reservation, Long finalRoomId) {
        String title ="예약이 변경되었습니다.";
        String message ="";
        String senderName ="";

        switch (user.getUserRole()) {
            case MODEL -> {
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getDesignerName() +" 디자이너";
                senderName = reservation.getDesignerName();
            }
            case DESIGNER -> {
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getModel().getNickname() + "님";
                senderName = reservation.getModel().getNickname();
            }
        }

        String notificationTypeDescription = NotificationType.SCHEDULE.toDisplayScheduleType(title);


        return NotificationData.chattingNotification(user.getId(), NotificationType.SCHEDULE, notificationTypeDescription, title, message, finalRoomId, senderName);
    }

    @Transactional
    public NotificationData createRemindNotification(User user, Reservation reservation) {
        String message = "";

        switch (user.getUserRole()) {
            case MODEL -> {
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getDesignerName() +" 디자이너";
            }
            case DESIGNER -> {
                message = parseStartTime(reservation.getDate(), reservation.getStartTime()) +" "+ reservation.getModel().getNickname() + "님";
            }
        }

        String title = "내일 예정된 모델 일정이 있어요.";

        String notificationTypeDescription = NotificationType.SCHEDULE.toDisplayScheduleType(title);


        NotificationData notificationData = NotificationData.otherNotification(user.getId(), NotificationType.SCHEDULE, notificationTypeDescription, title, message, reservation.getId());
        return notificationData;
    }


}
