package com.example.orders.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        String bucket,
        String endpoint,
        String region,
        String accessKey,
        String secretKey,
        int presignedUrlExpiration,
        boolean pathStyleAccessEnabled
) {}
