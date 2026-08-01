/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Asistencia
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Usuario
 *  com.pic21.repository.AsistenciaRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.stereotype.Repository
 */
package com.pic21.repository;

import com.pic21.domain.Asistencia;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AsistenciaRepository
extends JpaRepository<Asistencia, Long> {
    public boolean existsByReunionAndUsuario(Reunion var1, Usuario var2);

    @Query(value="SELECT a FROM Asistencia a JOIN FETCH a.usuario WHERE a.reunion = :reunion")
    public List<Asistencia> findByReunionWithDetails(@Param(value="reunion") Reunion var1);

    @Modifying
    @Query(value="DELETE FROM Asistencia a WHERE a.usuario.id = :usuarioId")
    public void deleteByUsuarioId(@Param(value="usuarioId") Long var1);
}

