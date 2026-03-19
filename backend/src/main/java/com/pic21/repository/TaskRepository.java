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

    /** Lista todas las tareas (ADMIN) con JOIN FETCH. */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.assignedTo u " +
           "JOIN FETCH t.meeting m " +
           "JOIN FETCH t.createdBy cb " +
           "ORDER BY t.createdAt DESC")
    List<Task> findAllWithDetails();

    /** Lista tareas de una reunión. */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.assignedTo u " +
           "JOIN FETCH t.meeting m " +
           "JOIN FETCH t.createdBy cb " +
           "WHERE t.meeting.id = :meetingId " +
           "ORDER BY t.createdAt ASC")
    List<Task> findByMeetingId(@Param("meetingId") Long meetingId);

    /** Lista tareas asignadas a un usuario (mis tareas). */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.meeting m " +
           "JOIN FETCH t.createdBy cb " +
           "WHERE t.assignedTo.id = :userId " +
           "ORDER BY t.createdAt DESC")
    List<Task> findByAssignedToId(@Param("userId") Long userId);

    /** Lista tareas creadas por un usuario (PROFESOR). */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.assignedTo u " +
           "JOIN FETCH t.meeting m " +
           "WHERE t.createdBy.id = :creatorId " +
           "ORDER BY t.createdAt DESC")
    List<Task> findByCreatedByIdOrderByCreatedAtDesc(@Param("creatorId") Long creatorId);

    /** Lista tareas pendientes de una reunión. */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.assignedTo u " +
           "JOIN FETCH t.meeting m " +
           "JOIN FETCH t.createdBy cb " +
           "WHERE t.meeting.id = :meetingId AND t.status = :status " +
           "ORDER BY t.createdAt ASC")
    List<Task> findByMeetingIdAndStatus(@Param("meetingId") Long meetingId, @Param("status") TaskStatus status);

    /** Verifica si ya existe una tarea para este estudiante en esta reunión. */
    boolean existsByMeetingIdAndAssignedToId(Long meetingId, Long assignedToId);

    boolean existsByMeetingIdAndAssignedToIdAndStatus(Long meetingId, Long assignedToId, TaskStatus status);

    /** Elimina todas las tareas de una reunión. */
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM tasks WHERE meeting_id = :meetingId", nativeQuery = true)
    void deleteByMeetingId(@org.springframework.data.repository.query.Param("meetingId") Long meetingId);
}
