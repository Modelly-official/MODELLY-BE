package modelly.modelly_be.domain.profile.dto.request;


import jakarta.validation.constraints.Size;

public record UpdateDesignerProfileRequest(

        @Size(max = 20)
        String nickname,

        @Size(max = 100)
        String intro,

        @Size(max = 50)
        String shop,

        @Size(max = 50)
        String addressLine1,

        @Size(max = 50)
        String addressLine2,

        @Size(max = 254)
        String profileImageUrl
) {}
