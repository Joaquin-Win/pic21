package com.pic21.repository;

import com.pic21.domain.AsignacionTarea;
import com.pic21.domain.EstadoTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionTareaRepository extends JpaRepository<AsignacionTarea, Long> {

    List<AsignacionTarea> findByUsuarioId(Long usuarioId);

    @Query("SELECT a FROM AsignacionTarea a JOIN FETCH a.usuario WHERE a.tarea.id = :tareaId")
    List<AsignacionTarea> findByTareaIdWithUsuario(@Param("tareaId") Long tareaId);

    boolean existsByTareaIdAndUsuarioId(Long tareaId, Long usuarioId);

    long countByTareaId(Long tareaId);

    long countByTareaIdAndEstado(Long tareaId, EstadoTarea estado);

    @Modifying
    @Query("DELETE FROM AsignacionTarea a WHERE a.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("UPDATE AsignacionTarea a SET a.score = :score, a.attempts = :attempts, a.estado = :estado WHERE a.id = :id")
    void updateQuizResult(@Param("id") Long id,
                          @Param("score") Integer score,
                          @Param("attempts") int attempts,
                          @Param("estado") EstadoTarea estado);

    @Modifying
    @Query("UPDATE AsignacionTarea a SET a.estado = :estado WHERE a.tarea.id = :tareaId AND a.estado = :currentEstado")
    int updateEstadoByTareaIdAndEstado(@Param("tareaId") Long tareaId,
                                       @Param("currentEstado") EstadoTarea currentEstado,
                                       @Param("estado") EstadoTarea estado);
}
