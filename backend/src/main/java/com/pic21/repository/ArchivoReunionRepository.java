/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.ArchivoReunion
 *  com.pic21.repository.ArchivoReunionRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.stereotype.Repository
 */
package com.pic21.repository;

import com.pic21.domain.ArchivoReunion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivoReunionRepository
extends JpaRepository<ArchivoReunion, Long> {
    public List<ArchivoReunion> findByReunionId(Long var1);

    public List<ArchivoReunion> findByReunionIdOrderByUploadedAtDesc(Long var1);

    @Modifying
    @Query(value="DELETE FROM ArchivoReunion a WHERE a.reunion.id = :reunionId")
    public void deleteByReunionId(@Param(value="reunionId") Long var1);
}

