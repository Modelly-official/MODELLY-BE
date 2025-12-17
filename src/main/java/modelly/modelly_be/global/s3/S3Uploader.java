package modelly.modelly_be.global.s3;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.domain.infra.presignedURL.dto.PresignedUrlListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    public PresignedUploadResponse generatePresignedUrl(String folder) {
        String fileName = UUID.randomUUID().toString();
        String key = folder + "/" + fileName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(putRequest)
                        .build();

        PresignedPutObjectRequest presigned =
                s3Presigner.presignPutObject(presignRequest);

        String imageUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

        return new PresignedUploadResponse(
                presigned.url().toString(),
                imageUrl
        );
    }

    public PresignedUrlListResponse generatePresignedUrlList(String folder, int fileCount) {
        String folderId = UUID.randomUUID().toString();

        List<PresignedUploadResponse> uploadResponses = new ArrayList<>();

        for (int i = 0; i < fileCount; i++) {
            String suffix = (i==0) ? "_main" : "_" +(i+1);

            String key = String.format("%s/%s/originals/%s%s", folder, folderId, folderId, suffix);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            PutObjectPresignRequest presignRequest =
                    PutObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(5))
                            .putObjectRequest(putRequest)
                            .build();

            PresignedPutObjectRequest presigned =
                    s3Presigner.presignPutObject(presignRequest);

            String imageUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

            uploadResponses.add(new PresignedUploadResponse(
                    presigned.url().toString(),
                    imageUrl
            ));
        }
        String thumbnailKey = String.format("%s/%s/thumbnails/%s_main", folder, folderId, folderId);
        String thumbnailUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + thumbnailKey;

        return new PresignedUrlListResponse(folderId, uploadResponses, thumbnailUrl);
    }

}
