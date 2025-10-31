package com.waira.waira_v2.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.waira.waira_v2.dao.RolDao;
import com.waira.waira_v2.entity.Rol;
import com.waira.waira_v2.repository.RolRepository;

@Repository
public class RolDaoImpl implements RolDao {

    private final RolRepository rolRepository;

    public RolDaoImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public Optional<Rol> findByNombreRol(String nombreRol) {
        return rolRepository.findByNombreRol(nombreRol);
    }
}
