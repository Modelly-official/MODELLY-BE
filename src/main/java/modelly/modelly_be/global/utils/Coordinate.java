package modelly.modelly_be.global.utils;

import jakarta.annotation.Nullable;

public record Coordinate(
        @Nullable
        Double latitude,
        @Nullable
        Double longitude
) {
}
