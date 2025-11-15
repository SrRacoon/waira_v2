package com.waira.waira_v2.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.waira.waira_v2.dto.RegistroDTO;
import com.waira.waira_v2.entity.Rol;
import com.waira.waira_v2.entity.TipoIdentificacion;
import com.waira.waira_v2.entity.Usuario;
import com.waira.waira_v2.repository.RolRepository;
import com.waira.waira_v2.repository.TipoIdentificacionRepository;
import com.waira.waira_v2.repository.UsuarioRepository;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    public Usuario registrarCliente(RegistroDTO registroDTO) {
        if (usuarioRepository.findByEmail(registroDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        if (usuarioRepository.findByNumeroIdentificacion(registroDTO.getDocumento()).isPresent()) {
            throw new IllegalArgumentException("El documento ya está registrado");
        }
        
        if (!registroDTO.getContraseña().equals(registroDTO.getConfirmContraseña())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        
        TipoIdentificacion tipo = tipoIdentificacionRepository.findByNombreTipoDocumento(registroDTO.getTipoDocumento())
            .orElseThrow(() -> new IllegalArgumentException("Tipo de documento inválido"));
        
        Usuario usuario = new Usuario();
        usuario.setNombres(registroDTO.getNombres());
        usuario.setApellidos(registroDTO.getApellidos());
        usuario.setNumeroIdentificacion(registroDTO.getDocumento());
        usuario.setTipoIdentificacion(tipo);
        usuario.setTelefono(registroDTO.getTelefono());
        usuario.setEmail(registroDTO.getEmail());
        usuario.setContrasena(registroDTO.getContraseña());
        usuario.setEstadoCuenta(true);
        
        Rol rolCliente = rolRepository.findByNombreRol("CLIENTE")
            .orElseThrow(() -> new IllegalArgumentException("Rol CLIENTE no existe"));
        usuario.setRoles(Arrays.asList(rolCliente));
        
        return usuarioRepository.save(usuario);
    }
    
    public Usuario login(String email, String contrasena) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));
        
        if (!usuario.getContrasena().equals(contrasena)) {
            throw new IllegalArgumentException("Email o contraseña incorrectos");
        }
        
        if (!usuario.getEstadoCuenta()) {
            throw new IllegalArgumentException("La cuenta está desactivada");
        }
        
        return usuario;
    }
}
