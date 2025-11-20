package com.waira.waira_v2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.SolicitudProveedor;
import com.waira.waira_v2.entity.Usuario;

@Repository
public interface SolicitudProveedorRepository extends JpaRepository<SolicitudProveedor, Integer> {
    Optional<SolicitudProveedor> findByUsuario(Usuario usuario);

    // Buscar la última solicitud por usuario (por id más alto)
    default Optional<SolicitudProveedor> findUltimaByUsuario(Usuario usuario) {
        List<SolicitudProveedor> todas = findAll();
        return todas.stream()
            .filter(s -> s.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .sorted((a, b) -> b.getIdSolicitud().compareTo(a.getIdSolicitud()))
            .findFirst();
    }
}
