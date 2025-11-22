package modelly.modelly_be.domain.auth.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.auth.dto.internal.DesignerExtra;
import modelly.modelly_be.domain.auth.dto.internal.ModelExtra;
import modelly.modelly_be.domain.auth.dto.internal.SignupBase;

@Getter
@AllArgsConstructor
public class SignupRequest {
    @Valid
    @NotNull
    private SignupBase base;

    @Valid
    private DesignerExtra designer;

    @Valid
    private ModelExtra model;
}
