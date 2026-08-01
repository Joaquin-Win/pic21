/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Tarea
 *  com.pic21.repository.TareaRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.stereotype.Repository
 */
package com.pic21.repository;

import com.pic21.domain.Tarea;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TareaRepository
extends JpaRepository<Tarea, Long> {
    public List<Tarea> findByReunionId(Long var1);

    public List<Tarea> findByCreadoPorIdOrderByCreatedAtDesc(Long var1);

    @Query(value="SELECT t FROM Tarea t JOIN FETCH t.reunion JOIN FETCH t.creadoPor")
    public List<Tarea> findAllWithDetails();
}

