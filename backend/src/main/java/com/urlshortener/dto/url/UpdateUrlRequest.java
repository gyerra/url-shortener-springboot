package com.urlshortener.dto.url;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateUrlRequest {

    @Size(max = 2048, message = "URL is too long")
    private String originalUrl;

    @Size(max = 255, message = "Title is too long")
    private String title;

    private Instant expiresAt;
}
