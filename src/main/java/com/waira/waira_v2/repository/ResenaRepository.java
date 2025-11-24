package com.waira.waira_v2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.waira.waira_v2.entity.Reseña;

public interface ResenaRepository extends JpaRepository<Reseña, Integer> {

    @Query("SELECT r.servicio.idServicio, AVG(r.calificacion), COUNT(r) " +
           "FROM Reseña r " +
           "WHERE r.calificacion IS NOT NULL " +
           "GROUP BY r.servicio.idServicio")
    List<Object[]> promedioYTotalPorServicio();
}
