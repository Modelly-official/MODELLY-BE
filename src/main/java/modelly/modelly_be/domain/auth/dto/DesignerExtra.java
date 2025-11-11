package modelly.modelly_be.domain.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import modelly.modelly_be.domain.user.entity.Category;

@Getter
@Setter
public class DesignerExtra {
    private String shop;
    private Category category;
}
