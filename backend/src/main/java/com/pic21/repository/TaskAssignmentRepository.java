package com.pic21.repository;

import com.pic21.domain.TaskAssignment;
import com.pic21.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    /** Asignaciones de una tarea general (con detalles de usuario). */
    @Query("SELECT a FROM TaskAssignment a JOIN FETCH a.assignedTo u WHERE a.task.id = :taskId ORDER BY u.lastName, u.firstName")
    List<TaskAssignment> findByTaskIdWithUser(@Param("taskId") Long taskId);

    /** Tareas asignadas a un usuario específico (mis tareas). */
    @Query("SELECT a FROM TaskAssignment a JOIN FETCH a.task t JOIN FETCH t.meeting m JOIN FETCH t.createdBy cb WHERE a.assignedTo.id = :userId ORDER BY a.createdAt DESC")
    List<TaskAssignment> findByAssignedToId(@Param("userId") Long userId);

    /** Verifica si ya existe asignación de esta tarea para este usuario. */
    boolean existsByTaskIdAndAssignedToId(Long taskId, Long userId);

    /** Cuenta de asignaciones por tarea. */
    long countByTaskId(Long taskId);

    /** Cuenta de asignaciones pendientes por tarea. */
    long countByTaskIdAndStatus(Long taskId, TaskStatus status);

    /** Elimina todas las asignaciones de un usuario. */
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM task_assignments WHERE user_id = :userId", nativeQuery = true)
    void deleteByAssignedToUserId(@Param("userId") Long userId);

    /** Actualiza score, attempts y status directamente via SQL nativo. Evita problemas de Hibernate con columnas nuevas. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE task_assignments SET score = :score, attempts = :attempts, status = :status WHERE id = :id", nativeQuery = true)
    void updateQuizResult(@Param("id") Long id, @Param("score") int score, @Param("attempts") int attempts, @Param("status") String status);
}

