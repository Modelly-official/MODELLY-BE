package modelly.modelly_be.domain.infra.presignedURL.dto;

import modelly.modelly_be.global.s3.PresignedUploadResponse;

import java.util.List;

public record PresignedUrlListResponse(
        String folderId, // 폴더 ID
        List<PresignedUploadResponse>presignedUrls, // 개별 파일 업로드 정보
        String thumbnailUrl
) {
}
