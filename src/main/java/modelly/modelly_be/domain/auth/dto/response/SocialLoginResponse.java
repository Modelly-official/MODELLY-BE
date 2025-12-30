package modelly.modelly_be.domain.auth.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.user.entity.enums.UserRole;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialLoginResponse {

    private Long userId;
    private String accessToken; // accessToken 반환
    private boolean registered; // true = 이미 가입된 회원
    private UserRole userRole;
    private String category;

    // 기존 회원 (바로 로그인 완료)
    public static SocialLoginResponse existing(Long userId, String accessToken, UserRole userRole, String category) {
        SocialLoginResponse response = new SocialLoginResponse();
        response.userId = userId;
        response.accessToken = accessToken;
        response.registered = true;
        response.userRole = userRole;
        response.category = category;
        return response;
    }

    // 신규 (추가 정보 입력 필요)
    public static SocialLoginResponse newUser(Long userId, String accessToken, UserRole userRole, String category) {
        SocialLoginResponse response = new SocialLoginResponse();
        response.userId = userId;
        response.accessToken = accessToken;
        response.registered = false;
        response.userRole = userRole;
        response.category = category;
        return response;
    }
}
