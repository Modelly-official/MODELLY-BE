package modelly.modelly_be.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
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
}
