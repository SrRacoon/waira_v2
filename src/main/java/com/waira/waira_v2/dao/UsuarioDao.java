package com.waira.waira_v2.dao;

import java.util.Optional;

import com.waira.waira_v2.entity.Usuario;

public interface UsuarioDao {
    boolean existsByEmail(String email);
    Optional<Usuario> findByEmail(String email);
    Usuario save(Usuario usuario);
}
