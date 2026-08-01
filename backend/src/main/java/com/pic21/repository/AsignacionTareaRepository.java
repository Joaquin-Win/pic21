/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.AsignacionTarea
 *  com.pic21.domain.EstadoTarea
 *  com.pic21.repository.AsignacionTareaRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.stereotype.Repository
 */
package com.pic21.repository;

import com.pic21.domain.AsignacionTarea;
import com.pic21.domain.EstadoTarea;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionTareaRepository
extends JpaRepository<AsignacionTarea, Long> {
    public List<AsignacionTarea> findByUsuarioId(Long var1);

    @Query(value="SELECT a FROM AsignacionTarea a JOIN FETCH a.usuario WHERE a.tarea.id = :tareaId")
    public List<AsignacionTarea> findByTareaIdWithUsuario(@Param(value="tareaId") Long var1);

    public boolean existsByTareaIdAndUsuarioId(Long var1, Long var2);

    public long countByTareaId(Long var1);

    public long countByTareaIdAndEstado(Long var1, EstadoTarea var2);

    @Modifying
    @Query(value="DELETE FROM AsignacionTarea a WHERE a.usuario.id = :usuarioId")
    public void deleteByUsuarioId(@Param(value="usuarioId") Long var1);

    @Modifying
    @Query(value="UPDATE AsignacionTarea a SET a.score = :score, a.attempts = :attempts, a.estado = :estado WHERE a.id = :id")
    public void updateQuizResult(@Param(value="id") Long var1, @Param(value="score") Integer var2, @Param(value="attempts") int var3, @Param(value="estado") EstadoTarea var4);

    @Modifying
    @Query(value="UPDATE AsignacionTarea a SET a.estado = :estado WHERE a.tarea.id = :tareaId AND a.estado = :currentEstado")
    public int updateEstadoByTareaIdAndEstado(@Param(value="tareaId") Long var1, @Param(value="currentEstado") EstadoTarea var2, @Param(value="estado") EstadoTarea var3);
}

