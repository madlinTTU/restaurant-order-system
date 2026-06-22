package com.example.orders.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pre-signed S3 URL for direct image upload")
public record ImageUploadUrlResponse(
        @Schema(description = "Pre-signed S3 URL for uploading the image via HTTP PUT")
        String uploadUrl
) {}
