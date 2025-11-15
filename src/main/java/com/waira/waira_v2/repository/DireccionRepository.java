package com.waira.waira_v2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.Direccion;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {
}
