package com.waira.waira_v2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.Servicio;
import com.waira.waira_v2.entity.Usuario;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
    List<Servicio> findByUsuario(Usuario usuario);
}
