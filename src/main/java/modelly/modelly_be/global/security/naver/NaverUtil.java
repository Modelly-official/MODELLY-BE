package modelly.modelly_be.global.security.naver;

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
public class NaverUtil {

    @Value("${security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Value("${security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;

    @Value("${security.oauth2.client.provider.naver.user-info-uri}")
    private String userInfoUri;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    // 인가코드 -> AccessToken
    public NaverDTO.OAuthToken requestToken(String code, String state) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);
        params.add("redirect_uri", redirectUri);

        String response = restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(params)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(response, NaverDTO.OAuthToken.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Naver access token", e);
        }
    }

    // AccessToken -> 프로필
    public NaverDTO.NaverProfile requestProfile(NaverDTO.OAuthToken token) {
        String response = restClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccess_token())
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(response, NaverDTO.NaverProfile.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Naver profile", e);
        }
    }
}
