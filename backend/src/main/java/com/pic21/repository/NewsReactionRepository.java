/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.NewsReaction
 *  com.pic21.domain.NewsReaction$ReactionType
 *  com.pic21.repository.NewsReactionRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Repository
 */
package com.pic21.repository;

import com.pic21.domain.NewsReaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsReactionRepository
extends JpaRepository<NewsReaction, Long> {
    public Optional<NewsReaction> findByNewsIdAndUsuarioId(Long var1, Long var2);

    public long countByNewsIdAndReactionType(Long var1, NewsReaction.ReactionType var2);

    public void deleteByNewsIdAndUsuarioId(Long var1, Long var2);
}

