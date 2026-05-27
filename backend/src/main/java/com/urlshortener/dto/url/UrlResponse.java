package com.urlshortener.dto.url;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponse {

    private Long id;
    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private Long clickCount;
    private String title;
    private Instant expiresAt;
    private boolean disabled;
    private Instant createdAt;
}
