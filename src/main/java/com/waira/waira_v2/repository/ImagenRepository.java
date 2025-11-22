package com.waira.waira_v2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.Imagen;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Integer> {
}
