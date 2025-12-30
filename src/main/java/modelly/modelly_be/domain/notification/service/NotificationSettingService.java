package modelly.modelly_be.domain.notification.service;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.dto.request.NotificationSettingRequest;
import modelly.modelly_be.domain.notification.entity.NotificationSetting;
import modelly.modelly_be.domain.notification.repository.NotificationSettingRepository;
import modelly.modelly_be.domain.user.entity.User;
import modelly.modelly_be.domain.user.service.UserService;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public NotificationSetting getNotificationSetting(User user) {
        return notificationSettingRepository.findByUser(user)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_NOTIFICATION_SETTING));
    }

    @Transactional
    public void updateNotificationSetting(User user, NotificationSettingRequest notificationSettingRequest) {
        NotificationSetting notificationSetting = getNotificationSetting(user);

        notificationSetting.updateNotificationSetting
                (notificationSettingRequest.chattingNotification(),
                notificationSettingRequest.reservationNotification(),
                notificationSettingRequest.scheduleNotification(),
                notificationSettingRequest.reviewNotification());

        user.updateNotificationSetting(notificationSetting);
        userService.save(user);
    }
}
