package com.urlshortener.controller;

import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@Tag(name = "Redirect", description = "Public short URL redirects")
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL and record analytics")
    public RedirectView redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        String userAgent = request.getHeader("User-Agent");
        String target = urlService.resolveRedirect(shortCode, ip, userAgent);
        RedirectView redirectView = new RedirectView(target);
        redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return redirectView;
    }
}
