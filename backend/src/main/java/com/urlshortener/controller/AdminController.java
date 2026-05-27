package com.urlshortener.controller;

import com.urlshortener.dto.admin.AdminUserResponse;
import com.urlshortener.dto.admin.DisableUrlRequest;
import com.urlshortener.dto.common.PageResponse;
import com.urlshortener.dto.url.UrlResponse;
import com.urlshortener.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public ResponseEntity<PageResponse<AdminUserResponse>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    @GetMapping("/urls")
    @Operation(summary = "List all URLs")
    public ResponseEntity<PageResponse<UrlResponse>> urls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUrls(page, size));
    }

    @PatchMapping("/urls/{id}/disable")
    @Operation(summary = "Enable or disable a URL")
    public ResponseEntity<UrlResponse> disableUrl(
            @PathVariable Long id,
            @Valid @RequestBody DisableUrlRequest request) {
        return ResponseEntity.ok(adminService.setUrlDisabled(id, request));
    }
}
