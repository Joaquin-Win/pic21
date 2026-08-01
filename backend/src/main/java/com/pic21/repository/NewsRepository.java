/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.News
 *  com.pic21.repository.NewsRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Repository
 */
package com.pic21.repository;

import com.pic21.domain.News;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository
extends JpaRepository<News, Long> {
    public List<News> findAllByOrderByCreatedAtDesc();

    public List<News> findByActiveTrueOrderByCreatedAtDesc();
}

