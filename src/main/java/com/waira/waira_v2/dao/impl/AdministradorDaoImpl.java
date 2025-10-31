package com.waira.waira_v2.dao.impl;

import org.springframework.stereotype.Repository;

import com.waira.waira_v2.dao.AdministradorDao;
import com.waira.waira_v2.entity.Administrador;
import com.waira.waira_v2.repository.AdministradorRepository;

@Repository
public class AdministradorDaoImpl implements AdministradorDao {

    private final AdministradorRepository administradorRepository;

    public AdministradorDaoImpl(AdministradorRepository administradorRepository) {
        this.administradorRepository = administradorRepository;
    }

    @Override
    public Administrador save(Administrador administrador) {
        return administradorRepository.save(administrador);
    }
}
