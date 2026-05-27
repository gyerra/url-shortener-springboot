package com.urlshortener.repository;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.UrlAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlAnalyticsRepository extends JpaRepository<UrlAnalytics, Long> {

    Page<UrlAnalytics> findByUrlOrderByClickedAtDesc(ShortUrl url, Pageable pageable);

    @Query("SELECT a.country, COUNT(a) FROM UrlAnalytics a WHERE a.url = :url AND a.country IS NOT NULL " +
           "GROUP BY a.country ORDER BY COUNT(a) DESC")
    List<Object[]> countByCountry(@Param("url") ShortUrl url, Pageable pageable);
}
