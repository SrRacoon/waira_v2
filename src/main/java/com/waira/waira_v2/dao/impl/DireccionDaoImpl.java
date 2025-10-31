package com.waira.waira_v2.dao.impl;

import org.springframework.stereotype.Repository;

import com.waira.waira_v2.dao.DireccionDao;
import com.waira.waira_v2.entity.Direccion;
import com.waira.waira_v2.repository.DireccionRepository;

@Repository
public class DireccionDaoImpl implements DireccionDao {

    private final DireccionRepository direccionRepository;

    public DireccionDaoImpl(DireccionRepository direccionRepository) {
        this.direccionRepository = direccionRepository;
    }

    @Override
    public Direccion save(Direccion direccion) {
        return direccionRepository.save(direccion);
    }
}
