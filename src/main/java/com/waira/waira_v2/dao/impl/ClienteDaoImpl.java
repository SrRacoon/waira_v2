package com.waira.waira_v2.dao.impl;

import org.springframework.stereotype.Repository;

import com.waira.waira_v2.dao.ClienteDao;
import com.waira.waira_v2.entity.Cliente;
import com.waira.waira_v2.repository.ClienteRepository;

@Repository
public class ClienteDaoImpl implements ClienteDao {

    private final ClienteRepository clienteRepository;

    public ClienteDaoImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }
}
