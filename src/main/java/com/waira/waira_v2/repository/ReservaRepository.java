package com.waira.waira_v2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    @Query("SELECT COUNT(r) FROM Reserva r JOIN r.estado e " +
           "WHERE UPPER(e.tipoEstado) = 'RESERVA' " +
           "AND UPPER(e.nombreEstado) IN :estados")
    long countReservasActivas(@Param("estados") List<String> estados);
}
