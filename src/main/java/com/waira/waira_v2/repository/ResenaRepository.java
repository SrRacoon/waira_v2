package com.waira.waira_v2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waira.waira_v2.entity.Reseña;
import com.waira.waira_v2.entity.Servicio;

public interface ResenaRepository extends JpaRepository<Reseña, Integer> {

    @Query("SELECT r.servicio.idServicio, AVG(r.calificacion), COUNT(r) " +
           "FROM Reseña r " +
           "WHERE r.calificacion IS NOT NULL " +
           "GROUP BY r.servicio.idServicio")
    List<Object[]> promedioYTotalPorServicio();

    @Query("SELECT AVG(r.calificacion), COUNT(r) " +
           "FROM Reseña r " +
           "WHERE r.calificacion IS NOT NULL " +
           "AND r.servicio.idServicio = :servicioId")
    Object[] resumenPorServicio(@Param("servicioId") Integer servicioId);

    List<Reseña> findByServicioOrderByFechaCreacionDesc(Servicio servicio);
}
