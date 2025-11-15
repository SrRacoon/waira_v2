package com.waira.waira_v2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.SolicitudProveedor;
import com.waira.waira_v2.entity.Usuario;

@Repository
public interface SolicitudProveedorRepository extends JpaRepository<SolicitudProveedor, Integer> {
    Optional<SolicitudProveedor> findByUsuario(Usuario usuario);
}
