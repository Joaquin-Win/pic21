/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Reunion
 *  com.pic21.repository.ReunionRepository
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Repository
 */
package com.pic21.repository;

import com.pic21.domain.Reunion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReunionRepository
extends JpaRepository<Reunion, Long> {
    public Page<Reunion> findAllByOrderByFechaInicioDesc(Pageable var1);
}

