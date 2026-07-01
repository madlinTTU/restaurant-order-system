package com.example.orders.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    private final StorageProperties props;

    @Bean
    public S3Client s3Client() {
        S3Client client = S3Client.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(credentials())
                .serviceConfiguration(s3Configuration())
                .endpointOverride(URI.create(props.endpoint()))
                .build();
        ensureBucketExists(client);
        return client;
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(credentials())
                .serviceConfiguration(s3Configuration())
                .endpointOverride(URI.create(props.publicEndpoint()))
                .build();
    }

    private void ensureBucketExists(S3Client client) {
        try {
            client.headBucket(req -> req.bucket(props.bucket()));
        } catch (NoSuchBucketException e) {
            client.createBucket(req -> req.bucket(props.bucket()));
            log.info("Created S3 bucket: {}", props.bucket());
        }
    }

    private StaticCredentialsProvider credentials() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.accessKey(), props.secretKey())
        );
    }

    private S3Configuration s3Configuration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(props.pathStyleAccessEnabled())
                .build();
    }
}
