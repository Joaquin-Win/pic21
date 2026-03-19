package com.pic21.repository;

import com.pic21.domain.Meeting;
import com.pic21.domain.MeetingStatus;
import com.pic21.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /** Listar reuniones por estado */
    Page<Meeting> findByStatus(MeetingStatus status, Pageable pageable);

    /** Listar reuniones creadas por un usuario */
    Page<Meeting> findByCreatedBy(User createdBy, Pageable pageable);

    /** Verificar si hay reuniones ACTIVAS (solo puede haber una a la vez — regla futura) */
    List<Meeting> findByStatus(MeetingStatus status);

    /** Eliminar reunión por ID con Native SQL (evita conflicto con persistence context) */
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM meetings WHERE id = :id", nativeQuery = true)
    void deleteMeetingById(@org.springframework.data.repository.query.Param("id") Long id);
}
