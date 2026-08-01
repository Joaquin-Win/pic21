/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.News
 *  com.pic21.domain.News$NewsBuilder
 *  com.pic21.domain.NewsReaction
 *  com.pic21.domain.Usuario
 *  jakarta.persistence.CascadeType
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.OneToMany
 *  jakarta.persistence.Table
 *  org.hibernate.annotations.CreationTimestamp
 */
package com.pic21.domain;

import com.pic21.domain.News;
import com.pic21.domain.NewsReaction;
import com.pic21.domain.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;

/*
 * Exception performing whole class analysis ignored.
 */
@Entity
@Table(name="news")
public class News {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=500)
    private String title;
    @Column(length=2000)
    private String description;
    @Column(name="image_url", length=1000)
    private String imageUrl;
    @Column(name="source_url", nullable=false, length=1000)
    private String sourceUrl;
    @Column(name="published_at")
    private LocalDateTime publishedAt;
    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="created_by", nullable=false)
    private Usuario createdBy;
    @Column(nullable=false)
    private boolean active;
    @OneToMany(mappedBy="news", cascade={CascadeType.ALL}, orphanRemoval=true)
    private List<NewsReaction> reactions;

    private static boolean $default$active() {
        return true;
    }

    private static List<NewsReaction> $default$reactions() {
        return new ArrayList<NewsReaction>();
    }

    public static NewsBuilder builder() {
        return new NewsBuilder();
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

    public Usuario getCreatedBy() {
        return this.createdBy;
    }

    public boolean isActive() {
        return this.active;
    }

    public List<NewsReaction> getReactions() {
        return this.reactions;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(Usuario createdBy) {
        this.createdBy = createdBy;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setReactions(List<NewsReaction> reactions) {
        this.reactions = reactions;
    }

    public News() {
        this.active = News.$default$active();
        this.reactions = News.$default$reactions();
    }

    public News(Long id, String title, String description, String imageUrl, String sourceUrl, LocalDateTime publishedAt, LocalDateTime createdAt, Usuario createdBy, boolean active, List<NewsReaction> reactions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.sourceUrl = sourceUrl;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.active = active;
        this.reactions = reactions;
    }
}

