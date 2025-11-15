package com.waira.waira_v2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waira.waira_v2.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByNumeroIdentificacion(String numeroIdentificacion);
    
    Optional<Usuario> findByTelefono(String telefono);
}
