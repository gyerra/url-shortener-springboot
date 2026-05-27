package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "url_analytics", indexes = {
        @Index(name = "idx_url_id", columnList = "url_id"),
        @Index(name = "idx_clicked_at", columnList = "clicked_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private ShortUrl url;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(length = 2)
    private String country;

    @CreationTimestamp
    @Column(name = "clicked_at", nullable = false, updatable = false)
    private Instant clickedAt;
}
