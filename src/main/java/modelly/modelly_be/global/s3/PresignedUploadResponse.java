package modelly.modelly_be.global.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUploadResponse {
    private String uploadUrl;   // PUT용 presigned URL
    private String imageUrl;    // 최종 접근용 URL
}
