package com.urlshortener.service;

import com.urlshortener.dto.url.UrlResponse;
import com.urlshortener.entity.ShortUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlMapper {

    @Value("${app.base-url}")
    private String baseUrl;

    public UrlResponse toResponse(ShortUrl url) {
        return UrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .clickCount(url.getClickCount())
                .title(url.getTitle())
                .expiresAt(url.getExpiresAt())
                .disabled(url.isDisabled())
                .createdAt(url.getCreatedAt())
                .build();
    }
}
