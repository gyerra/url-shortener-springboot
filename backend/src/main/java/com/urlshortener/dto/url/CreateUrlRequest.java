package com.urlshortener.dto.url;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL is too long")
    private String originalUrl;

    @Size(max = 255, message = "Title is too long")
    private String title;

    private Instant expiresAt;

    @Size(max = 12, message = "Custom short code is too long")
    private String customShortCode;
}
