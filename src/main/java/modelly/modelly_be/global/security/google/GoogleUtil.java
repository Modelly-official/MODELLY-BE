package modelly.modelly_be.global.security.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GoogleUtil {

    @Value("${security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;


    // 인가 코드 -> Google OAuth 토큰 요청
    public GoogleDTO.OAuthToken requestToken(String code, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        String response = restClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(params)
                .retrieve()
                .body(String.class);
        try {
            return objectMapper.readValue(response, GoogleDTO.OAuthToken.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Google OAuth token", e);
        }
    }

    // Access Token -> Google 사용자 프로필 조회
    public GoogleDTO.GoogleProfile requestProfile(GoogleDTO.OAuthToken oAuthToken) {
        String response = restClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + oAuthToken.getAccess_token())
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(response, GoogleDTO.GoogleProfile.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Google user profile", e);
        }
    }
}

