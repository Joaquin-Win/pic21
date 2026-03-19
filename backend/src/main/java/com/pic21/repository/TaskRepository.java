package com.pic21.repository;

import com.pic21.domain.Task;
import com.pic21.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Task.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Lista todas las tareas de una reunión con JOIN FETCH para evitar
     * LazyInitializationException.
     */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.assignedTo u " +
           "JOIN FETCH t.meeting m " +
           "WHERE t.meeting.id = :meetingId " +
           "ORDER BY t.createdAt ASC")
    List<Task> findByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * Lista todas las tareas asignadas a un usuario (mis tareas), con JOIN FETCH.
     */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.meeting m " +
           "JOIN FETCH t.createdBy cb " +
           "WHERE t.assignedTo.id = :userId " +
           "ORDER BY t.createdAt DESC")
    List<Task> findByAssignedToId(@Param("userId") Long userId);

    /**
     * Verifica si ya existe una tarea para este estudiante en esta reunión
     * (evitar asignar duplicados).
     */
    boolean existsByMeetingIdAndAssignedToId(Long meetingId, Long assignedToId);

    /**
     * Verifica si un estudiante ya tiene tarea en esta reunión con un estado dado.
     */
    boolean existsByMeetingIdAndAssignedToIdAndStatus(Long meetingId, Long assignedToId, TaskStatus status);

    /** Elimina todas las tareas de una reunión (Native SQL para compatibilidad Hibernate 6) */
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM tasks WHERE meeting_id = :meetingId", nativeQuery = true)
    void deleteByMeetingId(@org.springframework.data.repository.query.Param("meetingId") Long meetingId);
}
