package modelly.modelly_be.domain.auth.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import modelly.modelly_be.domain.auth.dto.internal.DesignerExtra;
import modelly.modelly_be.domain.auth.dto.internal.ModelExtra;
import modelly.modelly_be.domain.auth.dto.internal.SocialSignupBase;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialSignupRequest {

    @Valid
    @NotNull
    private SocialSignupBase base;

    @Valid
    private DesignerExtra designer;

    @Valid
    private ModelExtra model;
}