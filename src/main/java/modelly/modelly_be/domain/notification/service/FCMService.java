package modelly.modelly_be.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FcmTokenService fcmTokenService;

    @Transactional
    public void pushToFCM(NotificationData notificationData) {
        String fcmToken = fcmTokenService.getToken(notificationData.userId());

        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("FCM token is null or empty", notificationData.userId());
            return;
        }

        try {

            //각 알림에 맞게 데이터 생성
            Map<String, String> data = new HashMap<>();
            data.put("title", notificationData.title());
            data.put("body", notificationData.message());
            data.put("targetId", notificationData.targetId().toString());
            data.put("notificationType", notificationData.type().getDescription());
            data.put("typeDescription", notificationData.typeDescription());

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .putAllData(data)
                    .build();

            FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공");

        } catch (FirebaseMessagingException e) {
            log.error("❌ 알림 전송 실패", e);
        }
    }
}
