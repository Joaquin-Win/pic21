package com.pic21.repository;

import com.pic21.domain.Reunion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {

    Page<Reunion> findAllByOrderByFechaInicioDesc(Pageable pageable);
}
