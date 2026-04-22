package com.pic21.repository;

import com.pic21.domain.Asistencia;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    boolean existsByReunionAndUsuario(Reunion reunion, Usuario usuario);

    @Query("SELECT a FROM Asistencia a JOIN FETCH a.usuario WHERE a.reunion = :reunion")
    List<Asistencia> findByReunionWithDetails(@Param("reunion") Reunion reunion);

    @Modifying
    @Query("DELETE FROM Asistencia a WHERE a.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
}
