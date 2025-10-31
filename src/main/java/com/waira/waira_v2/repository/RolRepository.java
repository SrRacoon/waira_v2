package com.waira.waira_v2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waira.waira_v2.entity.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombreRol(String nombreRol);
}
