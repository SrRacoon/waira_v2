package com.waira.waira_v2.dao;

import java.util.Optional;

import com.waira.waira_v2.entity.Rol;

public interface RolDao {
    Optional<Rol> findByNombreRol(String nombreRol);
}
