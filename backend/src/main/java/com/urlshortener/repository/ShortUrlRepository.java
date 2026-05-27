package com.urlshortener.repository;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    Page<ShortUrl> findByCreatedBy(User user, Pageable pageable);

    @Query("SELECT u FROM ShortUrl u WHERE u.createdBy = :user AND " +
           "(LOWER(u.originalUrl) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.shortCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(u.title, '')) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ShortUrl> searchByUser(@Param("user") User user, @Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM ShortUrl u WHERE u.createdBy = :user ORDER BY u.clickCount DESC")
    List<ShortUrl> findTopByCreatedByOrderByClickCountDesc(@Param("user") User user, Pageable pageable);

    long countByCreatedBy(User user);

    @Query("SELECT COALESCE(SUM(u.clickCount), 0) FROM ShortUrl u WHERE u.createdBy = :user")
    long sumClickCountByUser(@Param("user") User user);
}
