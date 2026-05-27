package com.urlshortener.dto.url;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalLinks;
    private long totalClicks;
    private UrlResponse mostPopularUrl;
    private List<UrlResponse> recentUrls;
    private List<UrlResponse> topPerformingLinks;
}
