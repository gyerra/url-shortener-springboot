package com.urlshortener.service;

import com.urlshortener.dto.common.PageResponse;
import com.urlshortener.dto.url.*;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.UrlAnalytics;
import com.urlshortener.entity.User;
import com.urlshortener.exception.BadRequestException;
import com.urlshortener.exception.ForbiddenException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.repository.UrlAnalyticsRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.UserPrincipal;
import com.urlshortener.util.ShortCodeGenerator;
import com.urlshortener.util.UrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.urlshortener.config.RedisConfig.URL_CACHE;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UrlAnalyticsRepository analyticsRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlValidator urlValidator;
    private final UrlMapper urlMapper;
    private final CacheManager cacheManager;

    private static final String[] MOCK_COUNTRIES = {"US", "GB", "IN", "DE", "FR", "CA", "AU", "BR"};

    @Transactional
    @CacheEvict(value = URL_CACHE, allEntries = true)
    public UrlResponse createUrl(CreateUrlRequest request, UserPrincipal principal) {
        if (!urlValidator.isValid(request.getOriginalUrl())) {
            throw new BadRequestException("Invalid URL format");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String shortCode = resolveShortCode(request.getCustomShortCode());

        ShortUrl shortUrl = ShortUrl.builder()
                .originalUrl(urlValidator.normalize(request.getOriginalUrl()))
                .shortCode(shortCode)
                .title(request.getTitle())
                .expiresAt(request.getExpiresAt())
                .createdBy(user)
                .build();

        return urlMapper.toResponse(shortUrlRepository.save(shortUrl));
    }

    @Transactional(readOnly = true)
    public PageResponse<UrlResponse> getMyUrls(UserPrincipal principal, String search, int page, int size, String sortBy, String direction) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ShortUrl> urls = (search != null && !search.isBlank())
                ? shortUrlRepository.searchByUser(user, search.trim(), pageable)
                : shortUrlRepository.findByCreatedBy(user, pageable);

        Page<UrlResponse> mapped = urls.map(urlMapper::toResponse);
        return PageResponse.from(mapped);
    }

    @Transactional
    public UrlResponse updateUrl(Long id, UpdateUrlRequest request, UserPrincipal principal) {
        ShortUrl url = getOwnedUrl(id, principal);
        evictUrlCache(url.getShortCode());

        if (request.getOriginalUrl() != null) {
            if (!urlValidator.isValid(request.getOriginalUrl())) {
                throw new BadRequestException("Invalid URL format");
            }
            url.setOriginalUrl(urlValidator.normalize(request.getOriginalUrl()));
        }
        if (request.getTitle() != null) {
            url.setTitle(request.getTitle());
        }
        if (request.getExpiresAt() != null) {
            url.setExpiresAt(request.getExpiresAt());
        }

        return urlMapper.toResponse(shortUrlRepository.save(url));
    }

    @Transactional
    public void deleteUrl(Long id, UserPrincipal principal) {
        ShortUrl url = getOwnedUrl(id, principal);
        evictUrlCache(url.getShortCode());
        shortUrlRepository.delete(url);
    }

    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getAnalytics(Long id, UserPrincipal principal) {
        ShortUrl url = getOwnedUrl(id, principal);

        Page<UrlAnalytics> recent = analyticsRepository.findByUrlOrderByClickedAtDesc(
                url, PageRequest.of(0, 20));

        List<UrlAnalyticsResponse.ClickEvent> clicks = recent.getContent().stream()
                .map(a -> UrlAnalyticsResponse.ClickEvent.builder()
                        .ipAddress(maskIp(a.getIpAddress()))
                        .userAgent(a.getUserAgent())
                        .country(a.getCountry())
                        .clickedAt(a.getClickedAt())
                        .build())
                .collect(Collectors.toList());

        List<UrlAnalyticsResponse.CountryStat> countries = analyticsRepository
                .countByCountry(url, PageRequest.of(0, 10))
                .stream()
                .map(row -> UrlAnalyticsResponse.CountryStat.builder()
                        .country((String) row[0])
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        if (countries.isEmpty()) {
            countries = buildMockCountryStats(url.getClickCount());
        }

        return UrlAnalyticsResponse.builder()
                .urlId(url.getId())
                .shortCode(url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .clickCount(url.getClickCount())
                .recentClicks(clicks)
                .topCountries(countries)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long totalLinks = shortUrlRepository.countByCreatedBy(user);
        long totalClicks = shortUrlRepository.sumClickCountByUser(user);

        List<ShortUrl> topUrls = shortUrlRepository.findTopByCreatedByOrderByClickCountDesc(
                user, PageRequest.of(0, 5));

        Page<ShortUrl> recent = shortUrlRepository.findByCreatedBy(
                user, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));

        UrlResponse mostPopular = topUrls.isEmpty() ? null : urlMapper.toResponse(topUrls.get(0));

        return DashboardStatsResponse.builder()
                .totalLinks(totalLinks)
                .totalClicks(totalClicks)
                .mostPopularUrl(mostPopular)
                .recentUrls(recent.getContent().stream().map(urlMapper::toResponse).collect(Collectors.toList()))
                .topPerformingLinks(topUrls.stream().map(urlMapper::toResponse).collect(Collectors.toList()))
                .build();
    }

    @Cacheable(value = URL_CACHE, key = "#shortCode")
    @Transactional(readOnly = true)
    public ShortUrl findByShortCodeCached(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));
    }

    @Transactional
    public String resolveRedirect(String shortCode, String ip, String userAgent) {
        ShortUrl url = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));

        if (url.isDisabled()) {
            throw new ForbiddenException("This link has been disabled");
        }
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("This link has expired");
        }

        url.setClickCount(url.getClickCount() + 1);
        shortUrlRepository.save(url);

        String country = MOCK_COUNTRIES[(int) (Math.abs(shortCode.hashCode()) % MOCK_COUNTRIES.length)];
        UrlAnalytics analytics = UrlAnalytics.builder()
                .url(url)
                .ipAddress(ip)
                .userAgent(userAgent != null && userAgent.length() > 512
                        ? userAgent.substring(0, 512) : userAgent)
                .country(country)
                .build();
        analyticsRepository.save(analytics);
        evictUrlCache(shortCode);

        return url.getOriginalUrl();
    }

    public void evictUrlCache(String shortCode) {
        var cache = cacheManager.getCache(URL_CACHE);
        if (cache != null) {
            cache.evict(shortCode);
        }
    }

    private ShortUrl getOwnedUrl(Long id, UserPrincipal principal) {
        ShortUrl url = shortUrlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));
        if (!url.getCreatedBy().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new ForbiddenException("You do not have permission to access this URL");
        }
        return url;
    }

    private String resolveShortCode(String custom) {
        if (custom != null && !custom.isBlank()) {
            String code = custom.trim();
            if (!code.matches("^[a-zA-Z0-9_-]{3,12}$")) {
                throw new BadRequestException("Custom short code must be 3-12 alphanumeric characters");
            }
            if (shortUrlRepository.existsByShortCode(code)) {
                throw new BadRequestException("Short code already in use");
            }
            return code;
        }
        String code;
        do {
            code = shortCodeGenerator.generate();
        } while (shortUrlRepository.existsByShortCode(code));
        return code;
    }

    private String maskIp(String ip) {
        if (ip == null) return null;
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".xxx";
        }
        return "xxx";
    }

    private List<UrlAnalyticsResponse.CountryStat> buildMockCountryStats(long clicks) {
        if (clicks == 0) return List.of();
        return List.of(
                UrlAnalyticsResponse.CountryStat.builder().country("US").count(Math.max(1, clicks / 3)).build(),
                UrlAnalyticsResponse.CountryStat.builder().country("GB").count(Math.max(1, clicks / 4)).build(),
                UrlAnalyticsResponse.CountryStat.builder().country("IN").count(Math.max(1, clicks / 5)).build()
        );
    }
}
