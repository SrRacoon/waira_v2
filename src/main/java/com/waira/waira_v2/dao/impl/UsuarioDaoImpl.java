package com.waira.waira_v2.dao.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.waira.waira_v2.dao.UsuarioDao;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.UsuarioRepository;

@Repository
public class UsuarioDaoImpl implements UsuarioDao {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDaoImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
