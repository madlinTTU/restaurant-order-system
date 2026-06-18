package com.example.orders.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties props;

    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    public String generateKey(UUID itemId, String contentType) {
        String extension = ALLOWED_IMAGE_TYPES.get(contentType);
        if (extension == null) {
            throw new IllegalArgumentException("Unsupported image type: " + contentType);
        }
        return "menu/images/" + itemId + "/" + UUID.randomUUID() + "." + extension;
    }

    public String buildPublicUrl(String key) {
        return props.endpoint() + "/" + props.bucket() + "/" + key;
    }

    public String generatePresignedUploadUrl(String key) {
        PutObjectPresignRequest request = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(props.presignedUrlExpiration()))
                .putObjectRequest(req -> req.bucket(props.bucket()).key(key))
                .build();
        return s3Presigner.presignPutObject(request).url().toString();
    }

    public boolean objectExists(String key) {
        try {
            s3Client.headObject(req -> req.bucket(props.bucket()).key(key));
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    public String extractKey(String imageUrl) {
        String prefix = props.endpoint() + "/" + props.bucket() + "/";
        return imageUrl.replace(prefix, "");
    }
}
