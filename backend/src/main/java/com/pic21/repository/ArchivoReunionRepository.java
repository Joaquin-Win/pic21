package com.pic21.repository;

import com.pic21.domain.ArchivoReunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivoReunionRepository extends JpaRepository<ArchivoReunion, Long> {

    List<ArchivoReunion> findByReunionId(Long reunionId);

    List<ArchivoReunion> findByReunionIdOrderByUploadedAtDesc(Long reunionId);

    @Modifying
    @Query("DELETE FROM ArchivoReunion a WHERE a.reunion.id = :reunionId")
    void deleteByReunionId(@Param("reunionId") Long reunionId);
}
