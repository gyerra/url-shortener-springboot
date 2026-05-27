package com.urlshortener.dto.url;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlAnalyticsResponse {

    private Long urlId;
    private String shortCode;
    private String originalUrl;
    private Long clickCount;
    private List<ClickEvent> recentClicks;
    private List<CountryStat> topCountries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClickEvent {
        private String ipAddress;
        private String userAgent;
        private String country;
        private Instant clickedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountryStat {
        private String country;
        private Long count;
    }
}
