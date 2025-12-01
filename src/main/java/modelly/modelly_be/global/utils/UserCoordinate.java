package modelly.modelly_be.global.utils;

import jakarta.annotation.Nullable;

public record UserCoordinate(
        @Nullable
        Double userLat,
        @Nullable
        Double userLng
) {
}
