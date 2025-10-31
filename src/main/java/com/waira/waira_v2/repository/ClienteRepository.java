package com.waira.waira_v2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waira.waira_v2.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
