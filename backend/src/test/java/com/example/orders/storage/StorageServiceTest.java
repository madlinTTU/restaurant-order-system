package com.example.orders.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock S3Client s3Client;
    @Mock S3Presigner s3Presigner;

    StorageService storageService;

    StorageProperties props = new StorageProperties(
            "test-bucket",
            "http://localhost:9000",
            "us-east-1",
            "minioadmin",
            "minioadmin",
            3600,
            true
    );

    @BeforeEach
    void setup() {
        storageService = new StorageService(s3Client, s3Presigner, props);
    }

    @Test
    void generateKey_returnsCorrectFormatForJpeg() {
        UUID itemId = UUID.randomUUID();
        String key = storageService.generateKey(itemId, "image/jpeg");

        assertThat(key).startsWith("menu/images/" + itemId + "/");
        assertThat(key).endsWith(".jpg");
    }

    @Test
    void generateKey_returnsCorrectFormatForPng() {
        UUID itemId = UUID.randomUUID();
        String key = storageService.generateKey(itemId, "image/png");

        assertThat(key).endsWith(".png");
    }

    @Test
    void generateKey_returnsCorrectFormatForWebp() {
        UUID itemId = UUID.randomUUID();
        String key = storageService.generateKey(itemId, "image/webp");

        assertThat(key).endsWith(".webp");
    }

    @Test
    void generateKey_throwsWhenUnsupportedContentType() {
        assertThatThrownBy(() -> storageService.generateKey(UUID.randomUUID(), "video/mp4"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported image type");
    }

    @Test
    void buildPublicUrl_returnsCorrectUrl() {
        String url = storageService.buildPublicUrl("menu/images/file.jpg");

        assertThat(url).isEqualTo("http://localhost:9000/test-bucket/menu/images/file.jpg");
    }

    @Test
    void extractKey_returnsKeyFromUrl() {
        String url = "http://localhost:9000/test-bucket/menu/images/file.jpg";
        String key = storageService.extractKey(url);

        assertThat(key).isEqualTo("menu/images/file.jpg");
    }

}
