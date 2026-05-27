package com.urlshortener.service;

import com.urlshortener.dto.admin.AdminUserResponse;
import com.urlshortener.dto.admin.DisableUrlRequest;
import com.urlshortener.dto.common.PageResponse;
import com.urlshortener.dto.url.UrlResponse;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ShortUrlRepository shortUrlRepository;
    private final UrlMapper urlMapper;
    private final UrlService urlService;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getAllUsers(int page, int size) {
        Page<User> users = userRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        Page<AdminUserResponse> mapped = users.map(user -> AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .urlCount(shortUrlRepository.countByCreatedBy(user))
                .build());

        return PageResponse.from(mapped);
    }

    @Transactional(readOnly = true)
    public PageResponse<UrlResponse> getAllUrls(int page, int size) {
        Page<ShortUrl> urls = shortUrlRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponse.from(urls.map(urlMapper::toResponse));
    }

    @Transactional
    public UrlResponse setUrlDisabled(Long id, DisableUrlRequest request) {
        ShortUrl url = shortUrlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));
        url.setDisabled(Boolean.TRUE.equals(request.getDisabled()));
        ShortUrl saved = shortUrlRepository.save(url);
        urlService.evictUrlCache(saved.getShortCode());
        return urlMapper.toResponse(saved);
    }
}
