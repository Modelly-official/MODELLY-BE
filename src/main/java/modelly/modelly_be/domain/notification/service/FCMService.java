package modelly.modelly_be.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.notification.dto.internal.NotificationData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FCMService {

    @Transactional
    public void pushToFCM(NotificationData notification, String fcmToken) {
        try {
            //각 알림에 맞게 데이터 생성
            Map<String, String> data = new HashMap<>();
            switch (notification.type()) {
                case SCHEDULE -> {}
                case CHATTING -> {}
                case REVIEW -> {}
                case RESERVATION -> {}
            }

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .putAllData()
                    .build();

            FirebaseMessaging.getInstance().send(message);

        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
