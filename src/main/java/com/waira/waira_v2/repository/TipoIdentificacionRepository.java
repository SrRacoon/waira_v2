package com.waira.waira_v2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.TipoIdentificacion;

@Repository
public interface TipoIdentificacionRepository extends JpaRepository<TipoIdentificacion, Integer> {
    Optional<TipoIdentificacion> findByNombreTipoDocumento(String nombreTipoDocumento);
}
