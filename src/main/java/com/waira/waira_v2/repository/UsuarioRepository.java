package com.waira.waira_v2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waira.waira_v2.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
	Optional<Usuario> findByEmail(String email);
	boolean existsByEmail(String email);
}
