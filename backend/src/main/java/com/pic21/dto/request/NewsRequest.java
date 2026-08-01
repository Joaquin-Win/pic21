/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.request.NewsRequest
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Size
 */
package com.pic21.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class NewsRequest {
    @NotBlank(message="El t\u00edtulo es obligatorio")
    @Size(max=500)
    private @NotBlank(message="El t\u00edtulo es obligatorio") @Size(max=500) String title;
    @Size(max=2000)
    private @Size(max=2000) String description;
    @Size(max=1000)
    private @Size(max=1000) String imageUrl;
    @NotBlank(message="La URL de origen es obligatoria")
    @Size(max=1000)
    private @NotBlank(message="La URL de origen es obligatoria") @Size(max=1000) String sourceUrl;
    private LocalDateTime publishedAt;

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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}

