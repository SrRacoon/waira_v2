package com.waira.waira_v2.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waira.waira_v2.entity.Compra;

public interface CompraRepository extends JpaRepository<Compra, Integer> {

        @Query("SELECT COALESCE(COUNT(c), 0) " +
            "FROM Compra c JOIN c.estado e " +
            "WHERE UPPER(e.tipoEstado) = 'COMPRA' " +
            "AND UPPER(e.nombreEstado) IN ('COMPLETADA', 'COMPLETADO', 'CONFIRMADA', 'CONFIRMADO') " +
            "AND c.fechaCompra >= :inicio AND c.fechaCompra < :fin")
        Long totalComprasConfirmadasEntre(@Param("inicio") Date inicio, @Param("fin") Date fin);
}
