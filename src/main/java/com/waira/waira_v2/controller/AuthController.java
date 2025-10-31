package com.waira.waira_v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waira.waira_v2.repository.UsuarioRepository;

import com.waira.waira_v2.dto.AdministradorRegistroDto;
import com.waira.waira_v2.dto.AuthResponse;
import com.waira.waira_v2.dto.ClienteRegistroDto;
import com.waira.waira_v2.dto.LoginDto;
import com.waira.waira_v2.dto.ProveedorRegistroDto;
import com.waira.waira_v2.dto.RegistroUsuarioDto;
import com.waira.waira_v2.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistroUsuarioDto dto, HttpSession session) {
        AuthResponse r = usuarioService.registrarUsuario(dto);
        if (r.getIdUsuario() == null) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", r.getMensaje()));
        }

        // cargar usuario en sesión (sin contraseña)
        usuarioRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            u.setContrasena(null);
            session.setAttribute("usuarioLogueado", u);
        });

        return ResponseEntity.ok(java.util.Collections.singletonMap("redirect", "/"));
    }

    @PostMapping("/register/cliente")
    public ResponseEntity<?> registerCliente(@RequestBody ClienteRegistroDto dto, HttpSession session) {
        AuthResponse r = usuarioService.registrarCliente(dto);
        if (r.getIdUsuario() == null) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", r.getMensaje()));
        }

        usuarioRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            u.setContrasena(null);
            session.setAttribute("usuarioLogueado", u);
        });

        return ResponseEntity.ok(java.util.Collections.singletonMap("redirect", "/"));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody AdministradorRegistroDto dto, HttpSession session) {
        AuthResponse r = usuarioService.registrarAdministrador(dto);
        if (r.getIdUsuario() == null) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", r.getMensaje()));
        }

        usuarioRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            u.setContrasena(null);
            session.setAttribute("usuarioLogueado", u);
        });

        return ResponseEntity.ok(java.util.Collections.singletonMap("redirect", "/admin"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto, HttpSession session) {
        AuthResponse r = usuarioService.login(dto);
        if (r.getIdUsuario() == null) {
            return ResponseEntity.status(401).body(java.util.Collections.singletonMap("error", r.getMensaje()));
        }

        usuarioRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            u.setContrasena(null);
            session.setAttribute("usuarioLogueado", u);
        });

        return ResponseEntity.ok(java.util.Collections.singletonMap("redirect", "/"));
    }

    @PostMapping("/register/proveedor")
    public ResponseEntity<?> registerProveedor(@RequestBody ProveedorRegistroDto dto, HttpSession session) {
        AuthResponse r = usuarioService.registrarProveedor(dto);
        if (r.getIdUsuario() == null) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", r.getMensaje()));
        }

        usuarioRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            u.setContrasena(null);
            session.setAttribute("usuarioLogueado", u);
        });

        return ResponseEntity.ok(java.util.Collections.singletonMap("redirect", "/"));
    }
}
