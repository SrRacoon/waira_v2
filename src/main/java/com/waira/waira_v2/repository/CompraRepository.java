package com.waira.waira_v2.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waira.waira_v2.entity.Compra;

public interface CompraRepository extends JpaRepository<Compra, Integer> {

        @Query("SELECT COALESCE(SUM(r.cantidadPersonas * r.precioUnitario), 0) " +
            "FROM Compra c JOIN c.reserva r JOIN c.estado e " +
            "WHERE UPPER(e.nombreEstado) = 'CONFIRMADA' " +
            "AND UPPER(e.tipoEstado) = 'COMPRA' " +
            "AND c.fechaCompra >= :inicio AND c.fechaCompra < :fin")
    Double sumaIngresosConfirmadosEntre(@Param("inicio") Date inicio, @Param("fin") Date fin);
}
