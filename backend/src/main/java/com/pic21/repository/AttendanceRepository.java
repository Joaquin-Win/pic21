package com.pic21.repository;

import com.pic21.domain.Attendance;
import com.pic21.domain.Meeting;
import com.pic21.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Verifica si ya existe asistencia para el par (reunión, usuario).
     * Usado para validar duplicados antes de guardar.
     */
    boolean existsByMeetingAndUser(Meeting meeting, User user);

    /**
     * Lista todas las asistencias de una reunión, con JOIN FETCH para evitar
     * LazyInitializationException al mapear a DTO fuera de sesión.
     */
    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.user u " +
           "JOIN FETCH a.meeting m " +
           "WHERE a.meeting = :meeting " +
           "ORDER BY a.registeredAt ASC")
    List<Attendance> findByMeetingWithDetails(@Param("meeting") Meeting meeting);

    /** Lista simple por reunión, sin JOIN FETCH */
    List<Attendance> findByMeetingOrderByRegisteredAtAsc(Meeting meeting);

    /** Elimina todas las asistencias de una reunión por ID (Native SQL para compatibilidad Hibernate 6) */
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM attendances WHERE meeting_id = :meetingId", nativeQuery = true)
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}
