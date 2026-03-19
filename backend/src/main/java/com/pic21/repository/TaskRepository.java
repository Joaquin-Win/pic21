package com.pic21.repository;

import com.pic21.domain.Task;
import com.pic21.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Task (tarea general).
 * NOTA: Task ya NO tiene campo 'assignedTo' — las asignaciones están en TaskAssignment.
 *       'createdBy' es EAGER, no necesita JOIN FETCH.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Todas las tareas (ADMIN) — solo necesita meeting para mostrar el título.
     * createdBy es EAGER, no hay que hacer fetch explícito.
     */
    @Query("SELECT t FROM Task t JOIN FETCH t.meeting m ORDER BY t.createdAt DESC")
    List<Task> findAllWithDetails();

    /**
     * Tareas de una reunión específica.
     */
    @Query("SELECT t FROM Task t JOIN FETCH t.meeting m WHERE t.meeting.id = :meetingId ORDER BY t.createdAt ASC")
    List<Task> findByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * Tareas creadas por un usuario (PROFESOR).
     */
    @Query("SELECT t FROM Task t JOIN FETCH t.meeting m WHERE t.createdBy.id = :creatorId ORDER BY t.createdAt DESC")
    List<Task> findByCreatedByIdOrderByCreatedAtDesc(@Param("creatorId") Long creatorId);

    /**
     * Elimina todas las tareas de una reunión (CASCADE eliminará TaskAssignments).
     */
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM tasks WHERE meeting_id = :meetingId", nativeQuery = true)
    void deleteByMeetingId(@org.springframework.data.repository.query.Param("meetingId") Long meetingId);
}
