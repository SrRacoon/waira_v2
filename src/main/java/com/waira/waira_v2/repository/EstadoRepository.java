package com.waira.waira_v2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.Estado;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    Optional<Estado> findByNombreEstadoAndTipoEstado(String nombreEstado, String tipoEstado);
}
