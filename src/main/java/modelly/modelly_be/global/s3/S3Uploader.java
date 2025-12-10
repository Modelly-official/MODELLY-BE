package modelly.modelly_be.global.s3;

import lombok.RequiredArgsConstructor;
import modelly.modelly_be.global.apiPayload.code.status.ErrorStatus;
import modelly.modelly_be.global.apiPayload.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    public String upload(MultipartFile file, String dirName) {

        // 파일 크기 검증 (10MB)
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new GeneralException(ErrorStatus.FILE_TOO_LARGE);
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = dirName + "/" + UUID.randomUUID() + ext;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new GeneralException(ErrorStatus.FILE_UPLOAD_FAIL);
        }

        // S3 URL 만들기 (퍼블릭 버킷이라면 이런 식)
        String encodedKey = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + encodedKey;
    }

    public void delete(String fileUrl) {
        // 필요하면 나중에 구현 (URL → key로 변환해서 deleteObject)
    }
}
