package modelly.modelly_be.global.security.google;

import lombok.Getter;

public class GoogleDTO {

    @Getter
    public static class OAuthToken {
        private String access_token;   // 액세스 토큰
        private String expires_in;     // 만료 시간
        private String refresh_token;  // 리프레시 토큰
        private String scope;
        private String token_type;
        private String id_token;       // ID 토큰
    }

    @Getter
    public static class GoogleProfile {
        private String sub;            // 구글 계정 고유 ID
        private String email;
        private Boolean email_verified;
        private String name;
        private String given_name;
        private String family_name;
        private String picture;
        private String locale;
    }
}
