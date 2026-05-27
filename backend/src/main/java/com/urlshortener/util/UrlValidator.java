package com.urlshortener.util;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

@Component
public class UrlValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)([\\w\\-]+\\.)+[\\w\\-]+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?$",
            Pattern.CASE_INSENSITIVE);

    public boolean isValid(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String normalized = url.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        if (!URL_PATTERN.matcher(normalized).matches()) {
            return false;
        }
        try {
            URI uri = new URI(normalized);
            return uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public String normalize(String url) {
        String normalized = url.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        return normalized;
    }
}
