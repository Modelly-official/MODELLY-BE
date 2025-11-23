package modelly.modelly_be.global.security.kakao;

import lombok.Getter;

public class KakaoDTO {

    @Getter
    public static class OAuthToken {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private String scope;
        private int refresh_token_expires_in;
    }

    @Getter
    public static class KakaoProfile {
        private Long id;
        private String connected_at;
        private Properties properties;
        private KakaoAccount kakao_account;

        @Getter
        public static class Properties {
            private String nickname;
            private String profile_image;     // 필요하면
        }

        @Getter
        public static class KakaoAccount {
            private String email;
            private Boolean is_email_verified;
            private Boolean has_email;
            private Boolean profile_nickname_needs_agreement;
            private Boolean email_needs_agreement;
            private Boolean is_email_valid;
            private Profile profile;

            @Getter
            public static class Profile {
                private String nickname;
                private String profile_image_url;
                private Boolean is_default_nickname;
                private Boolean is_default_image;
            }
        }
    }
}
