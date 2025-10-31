package com.waira.waira_v2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waira.waira_v2.entity.Permiso;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {
    List<Permiso> findByAdministradorIdUsuario(Integer idUsuario);
}
