package com.urlshortener.controller;

import com.urlshortener.dto.common.PageResponse;
import com.urlshortener.dto.url.*;
import com.urlshortener.security.SecurityUtils;
import com.urlshortener.security.UserPrincipal;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Tag(name = "URLs", description = "Create and manage shortened URLs")
@SecurityRequirement(name = "bearerAuth")
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/create")
    @Operation(summary = "Create a shortened URL")
    public ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateUrlRequest request) {
        UserPrincipal user = SecurityUtils.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.createUrl(request, user));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's URLs with search, pagination, and sorting")
    public ResponseEntity<PageResponse<UrlResponse>> getMyUrls(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        UserPrincipal user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(urlService.getMyUrls(user, search, page, size, sortBy, direction));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<DashboardStatsResponse> dashboard() {
        return ResponseEntity.ok(urlService.getDashboardStats(SecurityUtils.getCurrentUser()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a shortened URL")
    public ResponseEntity<UrlResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateUrlRequest request) {
        return ResponseEntity.ok(urlService.updateUrl(id, request, SecurityUtils.getCurrentUser()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a shortened URL")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        urlService.deleteUrl(id, SecurityUtils.getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics/{id}")
    @Operation(summary = "Get analytics for a URL")
    public ResponseEntity<UrlAnalyticsResponse> analytics(@PathVariable Long id) {
        return ResponseEntity.ok(urlService.getAnalytics(id, SecurityUtils.getCurrentUser()));
    }
}
