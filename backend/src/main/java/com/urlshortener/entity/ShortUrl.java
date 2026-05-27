package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "urls", indexes = {
        @Index(name = "idx_short_code", columnList = "short_code", unique = true),
        @Index(name = "idx_created_by", columnList = "created_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 12)
    private String shortCode;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean disabled = false;

    @Column(length = 255)
    private String title;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "url", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UrlAnalytics> analytics = new ArrayList<>();
}
