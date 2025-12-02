package modelly.modelly_be.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import modelly.modelly_be.domain.user.entity.enums.UserRole;

@Getter
@Builder
public class OpponentInfoResponse {
    private Long userId;
    private String name;
    private String profileImageUrl;
    private UserRole role;
}
