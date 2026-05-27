package com.urlshortener.security;

import com.urlshortener.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return principal;
    }

    public static boolean isAdmin() {
        return getCurrentUser().getRole().name().equals("ADMIN");
    }
}
