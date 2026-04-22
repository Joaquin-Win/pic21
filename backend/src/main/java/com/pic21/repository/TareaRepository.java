package com.pic21.repository;

import com.pic21.domain.EstadoTarea;
import com.pic21.domain.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByReunionId(Long reunionId);

    List<Tarea> findByCreadoPorIdOrderByCreatedAtDesc(Long creadoPorId);

    @Query("SELECT t FROM Tarea t JOIN FETCH t.reunion JOIN FETCH t.creadoPor")
    List<Tarea> findAllWithDetails();
}
