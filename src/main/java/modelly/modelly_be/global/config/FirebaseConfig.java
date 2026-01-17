package modelly.modelly_be.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        InputStream serviceAccount = getClass().getResourceAsStream("/moandi_serviceAccountKey.json");

        if (serviceAccount == null) {
            throw new IllegalStateException("Firebase 서비스 계정 키를 찾을 수 없습니다");
        }

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) { // 기존에 FirebaseApp이 초기화되지 않은 경우만 실행
            FirebaseApp.initializeApp(options);
        }

    }
}
