package modelly.modelly_be.global.security.naver;

import lombok.Getter;
import lombok.Setter;

public class NaverDTO {

    @Getter
    public static class OAuthToken {
        private String access_token;
        private String refresh_token;
        private String token_type;
        private String expires_in;
        private String error;
        private String error_description;
    }

    @Getter
    public static class NaverProfile {
        private String resultcode;
        private String message;
        private Response response;

        @Getter
        public static class Response {
            private String id;          // 네이버 고유 id
            private String email;       // 선택 동의 필요
            private String name;        // 선택 동의
            private String nickname;    // 선택 동의
            private String profile_image;
        }
    }
}
