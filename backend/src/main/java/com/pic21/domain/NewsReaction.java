/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.News
 *  com.pic21.domain.NewsReaction
 *  com.pic21.domain.NewsReaction$NewsReactionBuilder
 *  com.pic21.domain.NewsReaction$ReactionType
 *  com.pic21.domain.Usuario
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EnumType
 *  jakarta.persistence.Enumerated
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.Table
 *  jakarta.persistence.UniqueConstraint
 *  org.hibernate.annotations.CreationTimestamp
 */
package com.pic21.domain;

import com.pic21.domain.News;
import com.pic21.domain.NewsReaction;
import com.pic21.domain.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name="news_reactions", uniqueConstraints={@UniqueConstraint(name="uk_news_reaction_user", columnNames={"news_id", "user_id"})})
public class NewsReaction {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="news_id", nullable=false)
    private News news;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private Usuario usuario;
    @Enumerated(value=EnumType.STRING)
    @Column(name="reaction_type", nullable=false, length=10)
    private ReactionType reactionType;
    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    public static NewsReactionBuilder builder() {
        return new NewsReactionBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public News getNews() {
        return this.news;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public ReactionType getReactionType() {
        return this.reactionType;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public NewsReaction() {
    }

    public NewsReaction(Long id, News news, Usuario usuario, ReactionType reactionType, LocalDateTime createdAt) {
        this.id = id;
        this.news = news;
        this.usuario = usuario;
        this.reactionType = reactionType;
        this.createdAt = createdAt;
    }
}

