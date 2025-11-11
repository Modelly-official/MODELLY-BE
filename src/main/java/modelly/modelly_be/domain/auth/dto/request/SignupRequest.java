package modelly.modelly_be.domain.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import modelly.modelly_be.domain.auth.dto.DesignerExtra;
import modelly.modelly_be.domain.auth.dto.SignupBase;

@Getter
@AllArgsConstructor
public class SignupRequest {
    private SignupBase base;
    private DesignerExtra designer; // role이 DESIGNER일 때만 채워서 전송
}
