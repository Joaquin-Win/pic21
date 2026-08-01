/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.response.NewsResponse
 *  com.pic21.dto.response.NewsResponse$NewsResponseBuilder
 */
package com.pic21.dto.response;

import com.pic21.dto.response.NewsResponse;
import java.time.LocalDateTime;

public class NewsResponse {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String sourceUrl;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private String createdByUsername;
    private boolean active;
    private long likes;
    private long dislikes;
    private String userReaction;

    NewsResponse(Long id, String title, String description, String imageUrl, String sourceUrl, LocalDateTime publishedAt, LocalDateTime createdAt, String createdByUsername, boolean active, long likes, long dislikes, String userReaction) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.sourceUrl = sourceUrl;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.createdByUsername = createdByUsername;
        this.active = active;
        this.likes = likes;
        this.dislikes = dislikes;
        this.userReaction = userReaction;
    }

    public static NewsResponseBuilder builder() {
        return new NewsResponseBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public String getSourceUrl() {
        return this.sourceUrl;
    }

    public LocalDateTime getPublishedAt() {
        return this.publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public String getCreatedByUsername() {
        return this.createdByUsername;
    }

    public boolean isActive() {
        return this.active;
    }

    public long getLikes() {
        return this.likes;
    }

    public long getDislikes() {
        return this.dislikes;
    }

    public String getUserReaction() {
        return this.userReaction;
    }
}

